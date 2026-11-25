package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.model.Content;
import br.com.studyassistant.studyassistantai.model.Subject;
import br.com.studyassistant.studyassistantai.model.Topic;
import br.com.studyassistant.studyassistantai.repository.ContentRepository;
import br.com.studyassistant.studyassistantai.repository.SubjectRepository;
import br.com.studyassistant.studyassistantai.repository.TopicRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;

class SourceSelectionServiceTest {

    private SubjectRepository subjectRepository;
    private TopicRepository topicRepository;
    private ContentRepository contentRepository;
    private SourceSelectionService service;

    @BeforeEach
    void setUp() {
        subjectRepository = mock(SubjectRepository.class);
        topicRepository = mock(TopicRepository.class);
        contentRepository = mock(ContentRepository.class);
        service = new SourceSelectionService(subjectRepository, topicRepository, contentRepository);
    }

    @Test
    void shouldAcceptEmptySelection() {
        assertDoesNotThrow(() -> service.validate(1L, null, null, null));
        verifyNoInteractions(subjectRepository, topicRepository, contentRepository);
    }

    @Test
    void shouldAcceptValidSelection() {
        Subject subject = createSubject(10L);
        Topic topic = createTopic(20L, subject);
        Content content = createContent(30L, topic);

        when(subjectRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(subject));
        when(topicRepository.findByIdAndUserId(20L, 1L)).thenReturn(Optional.of(topic));
        when(contentRepository.findByIdAndUserId(30L, 1L)).thenReturn(Optional.of(content));

        assertDoesNotThrow(() -> service.validate(1L, Set.of(10L), Set.of(20L), Set.of(30L)));
    }

    @Test
    void shouldRejectSubjectFromAnotherUser() {
        when(subjectRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> service.validate(1L, Set.of(10L), null, null));
    }

    @Test
    void shouldRejectTopicFromAnotherUser() {
        when(topicRepository.findByIdAndUserId(20L, 1L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> service.validate(1L, null, Set.of(20L), null));
    }

    @Test
    void shouldRejectContentFromAnotherUser() {
        when(contentRepository.findByIdAndUserId(30L, 1L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> service.validate(1L, null, null, Set.of(30L)));
    }

    @Test
    void shouldRejectTopicFromDifferentSubject() {
        Subject subject = createSubject(10L);
        Subject otherSubject = createSubject(11L);
        Topic topic = createTopic(20L, otherSubject);

        when(subjectRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(subject));
        when(topicRepository.findByIdAndUserId(20L, 1L)).thenReturn(Optional.of(topic));

        assertThrows(ResponseStatusException.class, () -> service.validate(1L, Set.of(10L), Set.of(20L), null));
    }

    @Test
    void shouldRejectContentFromDifferentTopic() {
        Subject subject = createSubject(10L);
        Topic selectedTopic = createTopic(20L, subject);
        Topic otherTopic = createTopic(21L, subject);
        Content content = createContent(30L, otherTopic);

        when(topicRepository.findByIdAndUserId(20L, 1L)).thenReturn(Optional.of(selectedTopic));
        when(contentRepository.findByIdAndUserId(30L, 1L)).thenReturn(Optional.of(content));

        assertThrows(ResponseStatusException.class, () -> service.validate(1L, null, Set.of(20L), Set.of(30L)));
    }

    @Test
    void shouldRejectMissingUserId() {
        assertThrows(IllegalArgumentException.class, () -> service.validate(null, null, null, null));
    }

    @Test
    void shouldRejectContentFromDifferentSubject() {
        Subject selectedSubject = createSubject(10L);
        Subject otherSubject = createSubject(11L);
        Topic topic = createTopic(20L, otherSubject);
        Content content = createContent(30L, topic);

        when(subjectRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(selectedSubject));
        when(contentRepository.findByIdAndUserId(30L, 1L)).thenReturn(Optional.of(content));

        assertThrows(
                ResponseStatusException.class, () -> service.validate(1L, Set.of(10L), null, Set.of(30L))
        );
    }

    private Subject createSubject(Long id) {
        Subject subject = new Subject();
        subject.setId(id);
        return subject;
    }

    private Topic createTopic(Long id, Subject subject) {
        Topic topic = new Topic();
        topic.setId(id);
        topic.setSubject(subject);
        return topic;
    }

    private Content createContent(Long id, Topic topic) {
        Content content = new Content();
        content.setId(id);
        content.setTopic(topic);
        return content;
    }
}