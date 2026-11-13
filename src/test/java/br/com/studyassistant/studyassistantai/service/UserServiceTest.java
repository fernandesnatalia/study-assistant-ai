package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.model.User;
import br.com.studyassistant.studyassistantai.repository.AnswerAttemptRepository;
import br.com.studyassistant.studyassistantai.repository.ContentRepository;
import br.com.studyassistant.studyassistantai.repository.DeckRepository;
import br.com.studyassistant.studyassistantai.repository.FlashcardRepository;
import br.com.studyassistant.studyassistantai.repository.NoteRepository;
import br.com.studyassistant.studyassistantai.repository.SubjectRepository;
import br.com.studyassistant.studyassistantai.repository.TopicRepository;
import br.com.studyassistant.studyassistantai.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AnswerAttemptRepository answerAttemptRepository;

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private FlashcardRepository flashcardRepository;

    @Mock
    private DeckRepository deckRepository;

    @Mock
    private ContentRepository contentRepository;

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @InjectMocks
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private User createUser() {
        User user = new User();
        user.setId(1L);
        user.setName("Augusta Ada Byron King");
        user.setEmail("ada@example.com");
        user.setPassword("ada123");
        return user;
    }

    @Test
    void shouldCreateUser() {
        User user = createUser();

        when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
        when(passwordEncoder.encode("ada123")).thenReturn("encoded-password");
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.createUser(user);

        assertSame(user, result);
        assertEquals("encoded-password", result.getPassword());
        verify(passwordEncoder).encode("ada123");
        verify(userRepository).save(user);
    }

    @Test
    void shouldRejectDuplicateEmail() {
        User user = createUser();

        when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> userService.createUser(user)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldGetAllUsers() {
        User user = createUser();

        when(userRepository.findAll()).thenReturn(List.of(user));

        List<User> result = userService.getAllUsers();

        assertEquals(1, result.size());
        assertSame(user, result.get(0));
    }

    @Test
    void shouldGetUserById() {
        User user = createUser();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertSame(user, userService.getUserById(1L));
    }

    @Test
    void shouldRejectNonexistentUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> userService.getUserById(1L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void shouldUpdateUser() {
        User user = createUser();
        User updated = createUser();
        updated.setName("Ada Lovelace");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.updateUser(1L, updated);

        assertEquals("Ada Lovelace", result.getName());
        assertEquals("ada@example.com", result.getEmail());
        verify(userRepository).save(user);
    }

    @Test
    void shouldRejectDuplicateEmailOnUpdate() {
        User user = createUser();
        User updated = createUser();
        updated.setEmail("other@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("other@example.com")).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> userService.updateUser(1L, updated)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldDeleteUser() {
        User user = createUser();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        verify(answerAttemptRepository).deleteByUserId(1L);
        verify(noteRepository).deleteByUserId(1L);
        verify(flashcardRepository).deleteByDeckUserId(1L);
        verify(deckRepository).deleteByUserId(1L);
        verify(contentRepository).deleteByUserId(1L);
        verify(topicRepository).deleteByUserId(1L);
        verify(subjectRepository).deleteByUserId(1L);
        verify(userRepository).delete(user);
    }

    @Test
    void shouldRejectDeletingNonexistentUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> userService.deleteUser(1L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(userRepository, never()).delete(any());
    }
}