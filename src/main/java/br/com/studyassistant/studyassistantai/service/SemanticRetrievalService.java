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
import static br.com.studyassistant.studyassistantai.constants.ErrorMessages.*;
import static br.com.studyassistant.studyassistantai.constants.SqlQueries.*;

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
            throw new IllegalArgumentException(SEARCH_REQUEST_REQUIRED);
        }

        if(userId == null){
            throw new IllegalArgumentException(USER_ID_REQUIRED);
        }

        if(request.query() == null || request.query().isBlank()){
            throw new IllegalArgumentException(QUERY_REQUIRED);
        }

        int topK = request.topK() == null ? DEFAULT_TOP_K : request.topK();
        double threshold = request.similarityThreshold() == null ? DEFAULT_THRESHOLD : request.similarityThreshold();

        if(topK <= 0){
            throw new IllegalArgumentException(INVALID_TOPK);
        }

        if(!(threshold >= 0 && threshold <= 1)){
            throw new IllegalArgumentException(INVALID_THRESHOLD);
        }

        sourceSelectionService.validate(userId, request.subjectsIds(), request.topicsIds(), request.contentsIds());

        String vector = toVectorLiteral(embeddingModel.embed(request.query()));

        StringBuilder sql = new StringBuilder(SEMANTIC_SEARCH_BASE);

        List<Object> parameters = new ArrayList<>(List.of(vector, userId, userId, userId, userId));

        appendFilter(sql, parameters, "s.id", request.subjectsIds());
        appendFilter(sql, parameters, "t.id", request.topicsIds());
        appendFilter(sql, parameters, "c.id", request.contentsIds());

        sql.append(SEMANTIC_SEARCH_SUFFIX);

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
                throw new IllegalStateException(INVALID_METADATA, e);
            }
        };
    }

    private String toVectorLiteral(float[] embedding){
        if(embedding == null || embedding.length == 0){
            throw new IllegalArgumentException(EMPTY_EMBEDDING);
        }

        StringJoiner joiner = new StringJoiner(",","[","]");

        for(float value : embedding) {
            joiner.add(Float.toString(value));
        }

        return joiner.toString();
    }
}