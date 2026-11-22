package br.com.studyassistant.studyassistantai.service.integration;

import br.com.studyassistant.studyassistantai.dto.RetrievedSource;
import br.com.studyassistant.studyassistantai.dto.SemanticSearchRequest;
import br.com.studyassistant.studyassistantai.model.Content;
import br.com.studyassistant.studyassistantai.model.Subject;
import br.com.studyassistant.studyassistantai.model.Topic;
import br.com.studyassistant.studyassistantai.model.User;
import br.com.studyassistant.studyassistantai.model.Document;
import br.com.studyassistant.studyassistantai.repository.DocumentRepository;
import br.com.studyassistant.studyassistantai.repository.ContentRepository;
import br.com.studyassistant.studyassistantai.repository.SubjectRepository;
import br.com.studyassistant.studyassistantai.repository.TopicRepository;
import br.com.studyassistant.studyassistantai.repository.UserRepository;
import br.com.studyassistant.studyassistantai.service.SemanticRetrievalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyString;

@SpringBootTest
@Transactional
class SemanticRetrievalIntegrationTest {

    @Autowired
    private SemanticRetrievalService retrievalService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private EmbeddingModel embeddingModel;

    private Long userId;
    private Long otherUserId;
    private Long subjectId;
    private Long topicId;
    private Long contentId;
    private Long secondContentId;

    @BeforeEach
    void setUp() {
        User user = createUser();
        User otherUser = createUser();

        userId = user.getId();
        otherUserId = otherUser.getId();

        Subject subject = createSubject(user, "Matemática");
        Topic topic = createTopic(user, subject, "Álgebra");

        subjectId = subject.getId();
        topicId = topic.getId();

        Content content = createContent(user, topic, "Anéis", "Conceitos de álgebra abstrata");
        Content secondContent = createContent(user, topic, "Grupos", "Conceitos de teoria dos grupos");

        contentId = content.getId();
        secondContentId = secondContent.getId();

        Subject otherSubject = createSubject(otherUser, "Física");
        Topic otherTopic = createTopic(otherUser, otherSubject, "Mecânica");

        Content otherContent = createContent(otherUser, otherTopic, "Movimento", "Leis do movimento");

        insertVector(contentId, userId, 0, "Trecho sobre anéis", 1);
        insertVector(secondContentId, userId, 1, "Trecho sobre grupos", 2);
        insertVector(otherContent.getId(), otherUserId, 0, "Trecho de outro usuário", 1);

        when(embeddingModel.embed(anyString())).thenReturn(createEmbedding(1));
    }

    @Test
    void shouldRetrieveRelevantSourcesWithMetadata() {
        List<RetrievedSource> results = retrievalService.retrieve(
                userId, request(null, null, null, 5, 0.7)
        );

        assertEquals(1, results.size());

        RetrievedSource source = results.get(0);

        assertEquals(contentId, source.contentId());
        assertEquals("Anéis", source.contentTitle());
        assertEquals(topicId, source.topicId());
        assertEquals("Álgebra", source.topicName());
        assertEquals(subjectId, source.subjectId());
        assertEquals("Matemática", source.subjectName());
        assertEquals(0, source.chunkIndex());
        assertEquals("Trecho sobre anéis", source.text());
        assertEquals(1.0, source.similarity(), 0.0001);

        assertNotNull(source.chunkId());
        assertNull(source.documentId());
    }

    @Test
    void shouldRespectTopK() {
        List<RetrievedSource> results = retrievalService.retrieve(
                userId, request(null, null, null, 1, 0.0)
        );

        assertEquals(1, results.size());
        assertEquals(contentId, results.get(0).contentId());
    }

    @Test
    void shouldApplyContentFilter() {
        List<RetrievedSource> results = retrievalService.retrieve(
                userId, request(null, null, Set.of(secondContentId), 5, 0.0)
        );

        assertEquals(1, results.size());
        assertEquals(secondContentId, results.get(0).contentId());
        assertEquals(1, results.get(0).chunkIndex());
    }

    @Test
    void shouldApplySubjectAndTopicFilters() {
        List<RetrievedSource> results = retrievalService.retrieve(
                userId, request(Set.of(subjectId), Set.of(topicId), Set.of(contentId), 5, 0.7)
        );

        assertEquals(1, results.size());
        assertEquals(contentId, results.get(0).contentId());
    }

    @Test
    void shouldReturnEmptyListWhenThresholdIsNotReached() {
        List<RetrievedSource> results = retrievalService.retrieve(
                userId, request(null, null, Set.of(secondContentId), 5, 0.7)
        );

        assertTrue(results.isEmpty());
    }

    @Test
    void shouldNotRetrieveSourcesFromAnotherUser() {
        List<RetrievedSource> results = retrievalService.retrieve(
                userId, request(null, null, null, 5, 0.0)
        );

        assertEquals(2, results.size());

        for (RetrievedSource source : results) {
            assertNotEquals("Trecho de outro usuário", source.text());
        }
    }

    @Test
    void shouldRejectSourceFromAnotherUser() {
        Long foreignContentId = jdbcTemplate.queryForObject(
                """
                SELECT id
                FROM content
                WHERE user_id = ?
                """,
                Long.class,
                otherUserId
        );

        assertNotNull(foreignContentId);

        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> retrievalService.retrieve(
                        userId, request(null, null, Set.of(foreignContentId), 5, 0.0)
                )
        );

        verify(embeddingModel, never()).embed(anyString());
    }

    @Test
    void shouldRetrieveDocumentSourceMetadata() {
        User user = userRepository.findById(userId).orElseThrow();

        Document document = new Document(user, "algebra.pdf", "PDF", 1024L);

        document = documentRepository.saveAndFlush(document);

        Content content = contentRepository.findById(contentId).orElseThrow();
        content.setDocument(document);
        contentRepository.saveAndFlush(content);

        List<RetrievedSource> results = retrievalService
                .retrieve(userId, request(null, null, Set.of(contentId), 5, 0.7));

        assertEquals(1, results.size());

        RetrievedSource source = results.get(0);

        assertEquals(contentId, source.contentId());
        assertEquals("Anéis", source.contentTitle());
        assertEquals(document.getId(), source.documentId());
        assertEquals("algebra.pdf", source.documentName());
        assertEquals(0, source.chunkIndex());
        assertEquals("Trecho sobre anéis", source.text());
    }

    private SemanticSearchRequest request(
            Set<Long> subjectIds,
            Set<Long> topicIds,
            Set<Long> contentIds,
            Integer topK,
            Double threshold
    ) {
        return new SemanticSearchRequest("Explique álgebra", subjectIds, topicIds, contentIds, null, topK, threshold);
    }

    private User createUser() {
        User user = new User();
        user.setName("Integration Test");
        user.setEmail(UUID.randomUUID() + "@example.com");
        user.setPassword("test-password");

        return userRepository.saveAndFlush(user);
    }

    private Subject createSubject(User user, String name) {
        Subject subject = new Subject();
        subject.setName(name);
        subject.setUser(user);

        return subjectRepository.saveAndFlush(subject);
    }

    private Topic createTopic(User user, Subject subject, String name) {
        Topic topic = new Topic();
        topic.setName(name);
        topic.setUser(user);
        topic.setSubject(subject);

        return topicRepository.saveAndFlush(topic);
    }

    private Content createContent(
            User user,
            Topic topic,
            String title,
            String body
    ) {
        Content content = new Content();
        content.setTitle(title);
        content.setBody(body);
        content.setUser(user);
        content.setTopic(topic);

        return contentRepository.saveAndFlush(content);
    }

    private void insertVector(
            Long contentId,
            Long userId,
            int chunkIndex,
            String text,
            int dimension
    ) {
        String metadata = """
                {"contentId": %d, "userId": %d, "chunkIndex": %d}
                """.formatted(contentId, userId, chunkIndex);

        float[] embedding = createEmbedding(dimension);

        List<String> values = new ArrayList<>();

        for (float value : embedding) {
            values.add(Float.toString(value));
        }

        String vector = "[" + String.join(",", values) + "]";

        jdbcTemplate.update(
                """
                INSERT INTO vector_store (id, content, metadata, embedding)
                VALUES (?, ?, CAST(? AS jsonb), CAST(? AS vector))
                """,
                UUID.randomUUID(),
                text,
                metadata,
                vector
        );
    }

    private float[] createEmbedding(int dimension) {
        float[] embedding = new float[768];
        embedding[dimension - 1] = 1.0f;
        return embedding;
    }
}