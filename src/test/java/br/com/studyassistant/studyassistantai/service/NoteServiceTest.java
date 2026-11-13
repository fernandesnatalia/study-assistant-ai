package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.model.Content;
import br.com.studyassistant.studyassistantai.model.Note;
import br.com.studyassistant.studyassistantai.model.Subject;
import br.com.studyassistant.studyassistantai.model.Topic;
import br.com.studyassistant.studyassistantai.model.User;
import br.com.studyassistant.studyassistantai.repository.ContentRepository;
import br.com.studyassistant.studyassistantai.repository.NoteRepository;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private ContentRepository contentRepository;

    @InjectMocks
    private NoteService noteService;

    private User user;
    private Subject subject;
    private Topic topic;
    private Content content;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        subject = new Subject();
        subject.setId(1L);

        topic = new Topic();
        topic.setId(1L);

        content = new Content();
        content.setId(1L);
    }

    @Test
    void shouldCreateNoteForSubject() {
        Note note = createNote();
        note.setSubject(subject);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(subjectRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(subject));
        when(noteRepository.existsByUserIdAndSubjectId(1L, 1L)).thenReturn(false);
        when(noteRepository.save(note)).thenReturn(note);

        Note result = noteService.create(note, 1L);

        assertSame(subject, result.getSubject());
        assertNull(result.getTopic());
        assertNull(result.getContent());
        assertSame(user, result.getUser());
        verify(noteRepository).save(note);
    }

    @Test
    void shouldCreateNoteForTopic() {
        Note note = createNote();
        note.setTopic(topic);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(topicRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(topic));
        when(noteRepository.existsByUserIdAndTopicId(1L, 1L)).thenReturn(false);
        when(noteRepository.save(note)).thenReturn(note);

        Note result = noteService.create(note, 1L);

        assertNull(result.getSubject());
        assertSame(topic, result.getTopic());
        assertNull(result.getContent());
        assertSame(user, result.getUser());
        verify(noteRepository).save(note);
    }

    @Test
    void shouldCreateNoteForContent() {
        Note note = createNote();
        note.setContent(content);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(contentRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(content));
        when(noteRepository.existsByUserIdAndContentId(1L, 1L)).thenReturn(false);
        when(noteRepository.save(note)).thenReturn(note);

        Note result = noteService.create(note, 1L);

        assertNull(result.getSubject());
        assertNull(result.getTopic());
        assertSame(content, result.getContent());
        assertSame(user, result.getUser());
        verify(noteRepository).save(note);
    }

    @Test
    void shouldRejectNoteWithoutContext() {
        Note note = createNote();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> noteService.create(note, 1L)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(noteRepository, never()).save(note);
    }

    @Test
    void shouldRejectNoteWithMultipleContexts() {
        Note note = createNote();
        note.setSubject(subject);
        note.setTopic(topic);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> noteService.create(note, 1L)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(noteRepository, never()).save(note);
    }

    @Test
    void shouldRejectDuplicateNoteForSubject() {
        Note note = createNote();
        note.setSubject(subject);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(subjectRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(subject));
        when(noteRepository.existsByUserIdAndSubjectId(1L, 1L)).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> noteService.create(note, 1L)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(noteRepository, never()).save(note);
    }

    @Test
    void shouldRejectDuplicateNoteForTopic() {
        Note note = createNote();
        note.setTopic(topic);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(topicRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(topic));
        when(noteRepository.existsByUserIdAndTopicId(1L, 1L)).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> noteService.create(note, 1L)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(noteRepository, never()).save(note);
    }

    @Test
    void shouldRejectDuplicateNoteForContent() {
        Note note = createNote();
        note.setContent(content);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(contentRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(content));
        when(noteRepository.existsByUserIdAndContentId(1L, 1L)).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> noteService.create(note, 1L)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(noteRepository, never()).save(note);
    }

    @Test
    void shouldRejectNoteForSubjectFromAnotherUser() {
        Note note = createNote();
        note.setSubject(subject);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(subjectRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> noteService.create(note, 1L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(noteRepository, never()).save(note);
    }

    @Test
    void shouldFindAllNotesByUser() {
        Note subjectNote = createNote();
        subjectNote.setSubject(subject);

        Note topicNote = createNote();
        topicNote.setTopic(topic);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(noteRepository.findByUserId(1L)).thenReturn(List.of(subjectNote, topicNote));

        List<Note> result = noteService.findAll(1L);

        assertEquals(2, result.size());
        assertSame(subjectNote, result.get(0));
        assertSame(topicNote, result.get(1));
    }

    @Test
    void shouldFindNoteByIdAndUser() {
        Note note = createNote();
        note.setId(1L);
        note.setSubject(subject);

        when(noteRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(note));

        Note result = noteService.findById(1L, 1L);

        assertSame(note, result);
    }

    @Test
    void shouldRejectAccessToNoteFromAnotherUser() {
        when(noteRepository.findByIdAndUserId(1L, 2L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> noteService.findById(1L, 2L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void shouldUpdateNoteTitleAndBody() {
        Note existingNote = createNote();
        existingNote.setId(1L);
        existingNote.setSubject(subject);

        Note updatedNote = new Note();
        updatedNote.setTitle("Anotações sobre Álgebra");
        updatedNote.setBody("Revisar as propriedades estudadas.");

        when(noteRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(existingNote));
        when(noteRepository.save(existingNote)).thenReturn(existingNote);

        Note result = noteService.update(1L, updatedNote, 1L);

        assertEquals("Anotações sobre Álgebra", result.getTitle());
        assertEquals("Revisar as propriedades estudadas.", result.getBody());
        assertSame(subject, result.getSubject());
        verify(noteRepository).save(existingNote);
    }

    @Test
    void shouldDeleteNote() {
        Note note = createNote();
        note.setId(1L);
        note.setContent(content);

        when(noteRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(note));

        noteService.delete(1L, 1L);

        verify(noteRepository).delete(note);
    }

    private Note createNote() {
        Note note = new Note();
        note.setTitle("Anotações de estudo");
        note.setBody("Pontos importantes para revisão.");
        return note;
    }
}