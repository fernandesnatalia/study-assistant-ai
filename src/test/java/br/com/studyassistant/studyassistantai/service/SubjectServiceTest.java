package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.model.Subject;
import br.com.studyassistant.studyassistantai.model.Topic;
import br.com.studyassistant.studyassistantai.model.User;
import br.com.studyassistant.studyassistantai.repository.SubjectRepository;
import br.com.studyassistant.studyassistantai.repository.TopicRepository;
import br.com.studyassistant.studyassistantai.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubjectServiceTest {

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SubjectService subjectService;

    private Subject subject;

    @BeforeEach
    void setUp() {
        subject = new Subject();
        subject.setId(1L);
        subject.setName("Matemática");
    }

    @Test
    void shouldCreateSubject() {
        User user = new User();
        user.setId(1L);

        subject.setUser(user);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(subjectRepository.save(subject)).thenReturn(subject);

        Subject result = subjectService.createSubject(subject);

        assertEquals("Matemática", result.getName());
        assertEquals(user, result.getUser());

        verify(userRepository).findById(1L);
        verify(subjectRepository).save(subject);
    }

    @Test
    void shouldGetAllSubjects() {
        when(subjectRepository.findByUserId(1L)).thenReturn(List.of(subject));

        List<Subject> result = subjectService.getAllSubjects(1L);

        assertEquals(1, result.size());
        assertEquals("Matemática", result.getFirst().getName());

        verify(subjectRepository).findByUserId(1L);
    }

    @Test
    void shouldGetSubjectById() {
        when(subjectRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(subject));

        Subject result = subjectService.getSubjectById(1L, 1L);

        assertEquals(1L, result.getId());
        assertEquals("Matemática", result.getName());
    }

    @Test
    void shouldReturnNotFoundWhenSubjectDoesNotExist() {
        when(subjectRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> subjectService.getSubjectById(99L, 1L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void shouldReturnBadRequestWhenUserIsMissing() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> subjectService.createSubject(subject)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verifyNoInteractions(userRepository, subjectRepository);
    }

    @Test
    void shouldReturnNotFoundWhenUserDoesNotExist() {
        User user = new User();
        user.setId(99L);

        subject.setUser(user);

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> subjectService.createSubject(subject)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(subjectRepository, never()).save(any(Subject.class));
    }

    @Test
    void shouldUpdateSubject() {
        Subject updatedSubject = new Subject();
        updatedSubject.setName("Matemática Aplicada");

        when(subjectRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(subject));
        when(subjectRepository.save(subject)).thenReturn(subject);

        Subject result = subjectService.updateSubject(1L, 1L, updatedSubject);

        assertEquals("Matemática Aplicada", result.getName());
        assertEquals(1L, result.getId());

        verify(subjectRepository).save(subject);
    }

    @Test
    void shouldDeleteSubject() {
        when(subjectRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(subject));

        subjectService.deleteSubject(1L, 1L);

        verify(subjectRepository).delete(subject);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonexistentSubject() {
        when(subjectRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> subjectService.deleteSubject(99L, 1L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(subjectRepository, never()).delete(any(Subject.class));
    }

    @Test
    void shouldAddTopicToSubject() {
        Topic topic = new Topic();
        topic.setId(10L);
        topic.setName("Algoritmos");

        when(subjectRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(subject));
        when(topicRepository.findById(10L)).thenReturn(Optional.of(topic));

        subjectService.addTopicToSubject(1L, 10L, 1L);

        assertTrue(subject.getTopics().contains(topic));
    }

    @Test
    void shouldGetTopicsFromSubject() {
        Topic topic = new Topic();
        topic.setId(10L);
        topic.setName("Algoritmos");

        subject.getTopics().add(topic);

        when(subjectRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(subject));

        Set<Topic> result = subjectService.getSubjectTopics(1L, 1L);

        assertEquals(1, result.size());
        assertTrue(result.contains(topic));
    }

    @Test
    void shouldRemoveTopicFromSubject() {
        Topic topic = new Topic();
        topic.setId(10L);
        topic.setName("Algoritmos");

        subject.getTopics().add(topic);

        when(subjectRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(subject));
        when(topicRepository.findById(10L)).thenReturn(Optional.of(topic));

        subjectService.removeTopicFromSubject(1L, 10L, 1L);

        assertTrue(subject.getTopics().isEmpty());
    }

    @Test
    void shouldNotFailWhenRemovingNonAssociatedTopic() {
        Topic topic = new Topic();
        topic.setId(10L);

        when(subjectRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(subject));
        when(topicRepository.findById(10L)).thenReturn(Optional.of(topic));

        assertDoesNotThrow(() -> subjectService.removeTopicFromSubject(1L, 10L, 1L));
    }

    @Test
    void shouldThrowNotFoundWhenAddingNonExistingTopic() {
        when(subjectRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(subject));
        when(topicRepository.findById(10L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> subjectService.addTopicToSubject(1L, 10L, 1L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void shouldNotGetAnotherUsersSubject() {
        when(subjectRepository.findByIdAndUserId(1L, 2L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> subjectService.getSubjectById(1L, 2L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void shouldNotUpdateAnotherUsersSubject() {
        Subject updatedSubject = new Subject();
        updatedSubject.setName("Álgebra Abstrata");

        when(subjectRepository.findByIdAndUserId(1L, 2L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> subjectService.updateSubject(1L, 2L, updatedSubject)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(subjectRepository, never()).save(any(Subject.class));
    }

    @Test
    void shouldNotDeleteAnotherUsersSubject() {
        when(subjectRepository.findByIdAndUserId(1L, 2L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> subjectService.deleteSubject(1L, 2L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(subjectRepository, never()).delete(any(Subject.class));
    }

    @Test
    void shouldNotAddTopicToAnotherUsersSubject() {
        when(subjectRepository.findByIdAndUserId(1L, 2L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> subjectService.addTopicToSubject(1L, 10L, 2L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verifyNoInteractions(topicRepository);
    }

    @Test
    void shouldNotGetTopicsFromAnotherUsersSubject() {
        when(subjectRepository.findByIdAndUserId(1L, 2L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> subjectService.getSubjectTopics(1L, 2L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verifyNoInteractions(topicRepository);
    }

    @Test
    void shouldNotRemoveTopicFromAnotherUsersSubject() {
        when(subjectRepository.findByIdAndUserId(1L, 2L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> subjectService.removeTopicFromSubject(1L, 10L, 2L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verifyNoInteractions(topicRepository);
    }
}