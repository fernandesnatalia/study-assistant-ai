package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.model.Content;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static br.com.studyassistant.studyassistantai.constants.ErrorMessages.CONTENT_NOT_PERSISTED;
import static br.com.studyassistant.studyassistantai.constants.SqlQueries.*;

@Service
public class ContentVectorService {

    private final VectorStore vectorStore;
    private final ContentChunkingService chunkingService;
    private final JdbcTemplate jdbcTemplate;

    public ContentVectorService(
            VectorStore vectorStore,
            ContentChunkingService chunkingService,
            JdbcTemplate jdbcTemplate
    ){
        this.vectorStore = vectorStore;
        this.chunkingService = chunkingService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void indexContent(Content content){
        if(content.getId() == null) {
            throw new IllegalArgumentException(CONTENT_NOT_PERSISTED);
        }

        List<String> chunks = chunkingService.split(content.getBody());
        List<Document> documents = new ArrayList<>();

        for(int i = 0; i < chunks.size(); i++) {
            String id = UUID.randomUUID().toString();

            Map<String, Object> metadata = Map.of(
                    "contentId", content.getId(),
                    "userId", content.getUser().getId(),
                    "chunkIndex", i
            );

            Document document = Document.builder()
                    .id(id)
                    .text(chunks.get(i))
                    .metadata(metadata)
                    .build();

            documents.add(document);
        }

        List<String> previousIds = jdbcTemplate.queryForList(
                FIND_CONTENT_VECTORS,
                String.class,
                content.getId().toString(),
                content.getUser().getId().toString()
        );

        vectorStore.add(documents);

        if(!previousIds.isEmpty()) {
            vectorStore.delete(previousIds);
        }
    }

    @Transactional
    public void deleteUserVectors(Long userId) {
        jdbcTemplate.update(DELETE_USER_VECTORS, userId.toString());
    }

    @Transactional
    public void deleteContentVectors(Long contentId, Long userId) {
        jdbcTemplate.update(DELETE_CONTENT_VECTORS, contentId.toString(), userId.toString());
    }
}