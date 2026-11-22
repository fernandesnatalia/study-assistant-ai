package br.com.studyassistant.studyassistantai.service.integration;

import br.com.studyassistant.studyassistantai.model.Content;
import br.com.studyassistant.studyassistantai.model.Subject;
import br.com.studyassistant.studyassistantai.model.Topic;
import br.com.studyassistant.studyassistantai.model.User;
import br.com.studyassistant.studyassistantai.repository.ContentRepository;
import br.com.studyassistant.studyassistantai.repository.SubjectRepository;
import br.com.studyassistant.studyassistantai.repository.TopicRepository;
import br.com.studyassistant.studyassistantai.repository.UserRepository;
import br.com.studyassistant.studyassistantai.service.ContentService;
import org.junit.jupiter.api.Test;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.UUID;
import static br.com.studyassistant.studyassistantai.constants.SqlQueries.DELETE_CONTENT_VECTORS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import java.util.Collections;
import java.util.List;

@SpringBootTest
class ContentVectorIntegrationTest {

    @Autowired
    private ContentService contentService;

    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @MockitoBean
    private VectorStore vectorStore;

    private static final String ORIGINAL_BODY = "Texto original para validar o rollback.";
    private static final String INDEXING_ERROR = "Vector indexing failed";

    @Test
    void shouldRollbackContentWhenVectorIndexingFails() {
        Long[] ids = transactionTemplate.execute(status -> {
            User user = new User();
            user.setName("Vector Integration Test");
            user.setEmail("vector-test-" + UUID.randomUUID() + "@example.com");
            user.setPassword("test-password");
            user = userRepository.saveAndFlush(user);

            Subject subject = new Subject();
            subject.setName("Matemática");
            subject.setUser(user);
            subject = subjectRepository.saveAndFlush(subject);

            Topic topic = new Topic();
            topic.setName("Álgebra");
            topic.setUser(user);
            topic.setSubject(subject);
            topic = topicRepository.saveAndFlush(topic);

            return new Long[] {user.getId(), subject.getId(), topic.getId()};
        });

        assertNotNull(ids);

        Long userId = ids[0];
        Long subjectId = ids[1];
        Long topicId = ids[2];

        try {
            Content content = new Content();
            content.setTitle("Conteúdo de teste");
            content.setBody("Conteúdo para validar o rollback da indexação.");

            Topic topic = new Topic();
            topic.setId(topicId);
            content.setTopic(topic);

            doThrow(new RuntimeException(INDEXING_ERROR)).when(vectorStore).add(anyList());

            assertThrows(RuntimeException.class, () -> contentService.createContent(userId, content));

            assertTrue(contentRepository.findByUserId(userId).isEmpty(), "Content must be rolled back when vector indexing fails");

            Long vectorCount = jdbcTemplate.queryForObject(
                    """
                    SELECT COUNT(*)
                    FROM vector_store
                    WHERE metadata ->> 'userId' = ?
                    """,
                    Long.class,
                    userId.toString()
            );

            assertEquals(0L, vectorCount);

        } finally {
            transactionTemplate.executeWithoutResult(status -> {
                topicRepository.deleteById(topicId);
                topicRepository.flush();

                subjectRepository.deleteById(subjectId);
                subjectRepository.flush();

                userRepository.deleteById(userId);
                userRepository.flush();
            });
        }
    }

    @Test
    void shouldRollbackContentAndVectorsWhenUpdateIndexingFails() {
        Long[] ids = transactionTemplate.execute(status -> {
            User user = new User();
            user.setName("Vector Update Test");
            user.setEmail("vector-update-" + UUID.randomUUID() + "@example.com");
            user.setPassword("test-password");
            user = userRepository.saveAndFlush(user);

            Subject subject = new Subject();
            subject.setName("Matemática");
            subject.setUser(user);
            subject = subjectRepository.saveAndFlush(subject);

            Topic topic = new Topic();
            topic.setName("Álgebra");
            topic.setUser(user);
            topic.setSubject(subject);
            topic = topicRepository.saveAndFlush(topic);

            Content content = new Content();
            content.setTitle("Conteúdo original");
            content.setBody(ORIGINAL_BODY);
            content.setUser(user);
            content.setTopic(topic);
            content = contentRepository.saveAndFlush(content);

            return new Long[] { user.getId(), subject.getId(), topic.getId(), content.getId() };
        });

        assertNotNull(ids);

        Long userId = ids[0];
        Long subjectId = ids[1];
        Long topicId = ids[2];
        Long contentId = ids[3];

        UUID vectorId = UUID.randomUUID();

        try {
            String embedding = "[" + String.join(",", Collections.nCopies(768, "0.1")) + "]";

            jdbcTemplate.update(
                    """
                    INSERT INTO vector_store (id, content, metadata, embedding)
                    VALUES (?, ?, ?::jsonb, ?::vector)
                    """,
                    vectorId,
                    ORIGINAL_BODY,
                    "{\"contentId\": %d, \"userId\": %d, \"chunkIndex\": 0}".formatted(contentId, userId),
                    embedding
            );

            Content updatedContent = new Content();
            updatedContent.setTitle("Conteúdo atualizado");
            updatedContent.setBody("Novo texto que não deve ser persistido.");

            doThrow(new RuntimeException(INDEXING_ERROR)).when(vectorStore).add(anyList());

            assertThrows(RuntimeException.class, () -> contentService.updateContent(contentId, userId, updatedContent));

            Content persistedContent = contentRepository.findByIdAndUserId(contentId, userId).orElseThrow();

            assertEquals("Conteúdo original", persistedContent.getTitle());
            assertEquals(ORIGINAL_BODY, persistedContent.getBody());

            List<UUID> vectorIds = jdbcTemplate.queryForList(
                    """ 
                    SELECT id 
                    FROM vector_store 
                    WHERE metadata ->> 'contentId' = ? 
                    AND metadata ->> 'userId' = ?
                    """,
                    UUID.class,
                    contentId.toString(),
                    userId.toString()
            );

            assertEquals(List.of(vectorId), vectorIds);

            verify(vectorStore).add(anyList());

        } finally {
            transactionTemplate.executeWithoutResult(status -> {
                jdbcTemplate.update(DELETE_CONTENT_VECTORS, contentId.toString(), userId.toString());

                contentRepository.deleteById(contentId);
                contentRepository.flush();

                topicRepository.deleteById(topicId);
                topicRepository.flush();

                subjectRepository.deleteById(subjectId);
                subjectRepository.flush();

                userRepository.deleteById(userId);
                userRepository.flush();
            });
        }
    }
}