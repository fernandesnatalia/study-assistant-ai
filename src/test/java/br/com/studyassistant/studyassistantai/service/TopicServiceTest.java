package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.model.Subject;
import br.com.studyassistant.studyassistantai.model.Topic;
import br.com.studyassistant.studyassistantai.model.User;
import br.com.studyassistant.studyassistantai.repository.TopicRepository;
import br.com.studyassistant.studyassistantai.repository.UserRepository;
import br.com.studyassistant.studyassistantai.repository.SubjectRepository;
import br.com.studyassistant.studyassistantai.repository.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.inOrder;

@ExtendWith(MockitoExtension.class)
class TopicServiceTest {

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentService documentService;

    @InjectMocks
    private TopicService topicService;

    private Topic topic;
    private Subject subject;
    private User ada;
    private User emmy;

    @BeforeEach
    void setUp() {
        ada = new User();
        ada.setId(1L);
        ada.setName("Ada Lovelace");

        emmy = new User();
        emmy.setId(2L);
        emmy.setName("Emmy Noether");

        topic = new Topic();
        topic.setId(1L);
        topic.setName("Sequências numéricas");
        topic.setUser(ada);

        subject = new Subject();
        subject.setId(1L);
        subject.setName("Matemática");
        subject.setUser(ada);
        topic.setSubject(subject);
    }

    @Test
    void shouldCreateTopic() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(ada));
        when(subjectRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(subject));
        when(topicRepository.save(topic)).thenReturn(topic);

        Topic result = topicService.createTopic(topic, 1L, 1L);

        assertEquals("Sequências numéricas", result.getName());
        assertEquals(ada, result.getUser());
        assertEquals(subject, result.getSubject());

        verify(userRepository).findById(1L);
        verify(subjectRepository).findByIdAndUserId(1L, 1L);
        verify(topicRepository).save(topic);
    }

    @Test
    void shouldReturnNotFoundWhenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> topicService.createTopic(topic, 99L, 1L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(topicRepository, never()).save(any(Topic.class));
    }

    @Test
    void shouldReturnNotFoundWhenSubjectDoesNotExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(ada));
        when(subjectRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> topicService.createTopic(topic, 1L, 99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(topicRepository, never()).save(any(Topic.class));
    }

    @Test
    void shouldGetAllTopics() {
        when(topicRepository.findByUserId(1L)).thenReturn(List.of(topic));

        List<Topic> result = topicService.getAllTopics(1L);

        assertEquals(1, result.size());
        assertEquals("Sequências numéricas", result.getFirst().getName());

        verify(topicRepository).findByUserId(1L);
    }

    @Test
    void shouldReturnOnlyTopicsFromRequestedUser() {
        Subject emmySubject = new Subject();
        emmySubject.setId(2L);
        emmySubject.setName("Álgebra");
        emmySubject.setUser(emmy);

        Topic emmyTopic = new Topic();
        emmyTopic.setId(2L);
        emmyTopic.setName("Anéis comutativos");
        emmyTopic.setUser(emmy);
        emmyTopic.setSubject(emmySubject);

        when(topicRepository.findByUserId(2L)).thenReturn(List.of(emmyTopic));

        List<Topic> result = topicService.getAllTopics(2L);

        assertEquals(1, result.size());
        assertEquals("Anéis comutativos", result.getFirst().getName());
        assertEquals(emmy, result.getFirst().getUser());

        verify(topicRepository).findByUserId(2L);
    }

    @Test
    void shouldGetTopicById() {
        when(topicRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(topic));

        Topic result = topicService.getTopicById(1L, 1L);

        assertEquals(1L, result.getId());
        assertEquals("Sequências numéricas", result.getName());
    }

    @Test
    void shouldReturnNotFoundWhenTopicDoesNotExist() {
        when(topicRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> topicService.getTopicById(99L, 1L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void shouldReturnNotFoundWhenTopicBelongsToAnotherUser() {
        when(topicRepository.findByIdAndUserId(1L, 2L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> topicService.getTopicById(1L, 2L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void shouldUpdateTopic() {
        Topic updatedTopic = new Topic();
        updatedTopic.setName("Algoritmos e sequências numéricas");

        when(topicRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(topic));
        when(topicRepository.save(topic)).thenReturn(topic);

        Topic result = topicService.updateTopic(1L, 1L, updatedTopic);

        assertEquals("Algoritmos e sequências numéricas", result.getName());
        assertEquals(ada, result.getUser());

        verify(topicRepository).save(topic);
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingAnotherUsersTopic() {
        Topic updatedTopic = new Topic();
        updatedTopic.setName("Anéis comutativos");

        when(topicRepository.findByIdAndUserId(1L, 2L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> topicService.updateTopic(1L, 2L, updatedTopic)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Sequências numéricas", topic.getName());

        verify(topicRepository, never()).save(any(Topic.class));
    }

    @Test
    void shouldDeleteTopic() {
        when(topicRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(topic));

        topicService.deleteTopic(1L, 1L);

        verify(topicRepository).delete(topic);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingAnotherUsersTopic() {
        when(topicRepository.findByIdAndUserId(1L, 2L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> topicService.deleteTopic(1L, 2L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(topicRepository, never()).delete(any(Topic.class));
    }

    @Test
    void shouldCleanOrphanDocumentsWhenDeletingTopic() {
        when(topicRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(topic));
        when(documentRepository.findDocumentIdsByTopicIdAndUserId(1L, 1L)).thenReturn(List.of(10L));

        topicService.deleteTopic(1L, 1L);

        InOrder order = inOrder(topicRepository, documentService);

        order.verify(topicRepository).delete(topic);
        order.verify(topicRepository).flush();
        order.verify(documentService).deleteOrphanDocuments(List.of(10L), 1L);
    }
}