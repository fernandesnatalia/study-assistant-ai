package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.model.Subject;
import br.com.studyassistant.studyassistantai.model.Topic;
import br.com.studyassistant.studyassistantai.model.User;
import br.com.studyassistant.studyassistantai.model.Content;
import br.com.studyassistant.studyassistantai.repository.DocumentRepository;
import br.com.studyassistant.studyassistantai.repository.SubjectRepository;
import br.com.studyassistant.studyassistantai.repository.TopicRepository;
import br.com.studyassistant.studyassistantai.repository.UserRepository;
import br.com.studyassistant.studyassistantai.repository.ContentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.support.TransactionTemplate;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
class DocumentServiceIntegrationTest {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @MockitoSpyBean
    private ContentRepository contentRepository;

    @Test
    void shouldRollbackDocumentWhenContentPersistenceFails() {
        Long[] ids = transactionTemplate.execute(status -> {
            User user = new User();
            user.setName("Document Integration Test");
            user.setEmail("document-test-" + UUID.randomUUID() + "@example.com");
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

            return new Long[]{user.getId(), subject.getId(), topic.getId()};
        });

        assertNotNull(ids);

        Long userId = ids[0];
        Long subjectId = ids[1];
        Long topicId = ids[2];

        try {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "material.txt",
                    "text/plain",
                    "Conteúdo original".getBytes(StandardCharsets.UTF_8)
            );

            assertTrue(documentRepository.findByUserId(userId).isEmpty());

            doThrow(new IllegalStateException("Simulated content persistence failure"))
                    .when(contentRepository)
                    .save(any(Content.class));

            assertThrows(
                    IllegalStateException.class,
                    () -> documentService.confirmDocument(
                            userId, file, topicId, "Material de estudo", "Conteúdo revisado"
                    )
            );

            verify(contentRepository).save(any(Content.class));

            assertTrue(
                    documentRepository.findByUserId(userId).isEmpty(),
                    "Document must be rolled back when Content persistence fails"
            );

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
}