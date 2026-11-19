package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.dto.RetrievedSource;
import br.com.studyassistant.studyassistantai.dto.SemanticSearchRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.StringJoiner;

@Service
public class SemanticRetrievalService {

    private static final int DEFAULT_TOP_K = 5;
    private static final double DEFAULT_THRESHOLD = 0.7;

    private final EmbeddingModel embeddingModel;
    private final JdbcTemplate jdbcTemplate;
    private final SourceSelectionService sourceSelectionService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SemanticRetrievalService(
            EmbeddingModel embeddingModel,
            JdbcTemplate jdbcTemplate,
            SourceSelectionService sourceSelectionService
    ){
        this.embeddingModel = embeddingModel;
        this.jdbcTemplate = jdbcTemplate;
        this.sourceSelectionService = sourceSelectionService;
    }

    @Transactional(readOnly = true)
    public List<RetrievedSource> retrieve(
            Long userId,
            SemanticSearchRequest request
    ) {
        if(request == null){
            throw new IllegalArgumentException("Search request is required");
        }

        if(userId == null){
            throw new IllegalArgumentException("User ID is required");
        }

        if(request.query() == null || request.query().isBlank()){
            throw new IllegalArgumentException("Query is required");
        }

        int topK = request.topK() == null ? DEFAULT_TOP_K : request.topK();
        double threshold = request.similarityThreshold() == null ? DEFAULT_THRESHOLD : request.similarityThreshold();

        if(topK <= 0){
            throw new IllegalArgumentException("Top K must be positive");
        }

        if(!(threshold >= 0 && threshold <= 1)){
            throw new IllegalArgumentException("Similarity thresold must be between 0 and 1");
        }

        sourceSelectionService.validate(userId, request.subjectsIds(), request.topicsIds(), request.contentsIds());

        String vector = toVectorLiteral(embeddingModel.embed(request.query()));

        StringBuilder sql = new StringBuilder("""
        SELECT
            v.id,
            v.content,
            v.metadata,
            1 - (v.embedding <=> CAST(? AS vector)) AS similarity,
            c.id AS content_id,
            c.title AS content_title,
            t.id AS topic_id,
            t.name AS topic_name,
            s.id AS subject_id,
            s.name AS subject_name,
            d.id AS document_id,
            d.original_filename AS document_filename
        FROM vector_store v
        JOIN content c
            ON c.id = (v.metadata ->> 'contentId')::bigint
        JOIN topic t
            ON t.id = c.topic_id
        JOIN subject s
            ON s.id = t.subject_id
        LEFT JOIN documents d
            ON d.id = c.document_id
        WHERE c.user_id = ?
        AND t.user_id = ?
        AND s.user_id = ?
        AND (v.metadata ->> 'userId')::bigint = ?
        """);

        List<Object> parameters = new ArrayList<>(List.of(vector, userId, userId, userId, userId));

        appendFilter(sql, parameters, "s.id", request.subjectsIds());
        appendFilter(sql, parameters, "t.id", request.topicsIds());
        appendFilter(sql, parameters, "c.id", request.contentsIds());

        sql.append("""
                AND 1 - (v.embedding <=> CAST(? AS vector)) >= ?
                ORDER BY similarity DESC, v.id
                LIMIT ?
                """);

        parameters.add(vector);
        parameters.add(threshold);
        parameters.add(topK);

        return jdbcTemplate.query(sql.toString(), sourceRowMapper(), parameters.toArray());
    }

    private void appendFilter(
            StringBuilder sql,
            List<Object> parameters,
            String column,
            Set<Long> ids
    ){
        if(ids == null || ids.isEmpty()) {
            return;
        }

        String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));

        sql.append(" AND ")
                .append(column)
                .append(" IN (")
                .append(placeholders)
                .append(")");

        parameters.addAll(ids);
    }

    private RowMapper<RetrievedSource> sourceRowMapper(){
        return(rs, rowNum) -> {
            try {
                JsonNode metadata = objectMapper.readTree(rs.getString("metadata"));
                return new RetrievedSource(
                        rs.getString("id"),
                        metadata.path("chunkIndex").asInt(),
                        rs.getString("content"),
                        rs.getDouble("similarity"),
                        rs.getLong("content_id"),
                        rs.getString("content_title"),
                        rs.getLong("topic_id"),
                        rs.getString("topic_name"),
                        rs.getLong("subject_id"),
                        rs.getString("subject_name"),
                        rs.getObject("document_id", Long.class),
                        rs.getString("document_filename")
                );
            } catch(JsonProcessingException e) {
                throw new IllegalStateException("Invalid vector metadata", e);
            }
        };
    }

    private String toVectorLiteral(float[] embedding){
        if(embedding == null || embedding.length == 0){
            throw new IllegalArgumentException("Embedding is empty");
        }

        StringJoiner joiner = new StringJoiner(",","[","]");

        for(float value : embedding) {
            joiner.add(Float.toString(value));
        }

        return joiner.toString();
    }
}