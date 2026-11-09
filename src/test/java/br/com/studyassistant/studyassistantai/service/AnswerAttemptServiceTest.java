package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.dto.AnswerAttemptRequest;
import br.com.studyassistant.studyassistantai.dto.AnswerAttemptResponse;
import br.com.studyassistant.studyassistantai.model.AnswerAttempt;
import br.com.studyassistant.studyassistantai.model.Flashcard;
import br.com.studyassistant.studyassistantai.model.User;
import br.com.studyassistant.studyassistantai.repository.AnswerAttemptRepository;
import br.com.studyassistant.studyassistantai.repository.FlashcardRepository;
import br.com.studyassistant.studyassistantai.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnswerAttemptServiceTest {

    @Mock
    private AnswerAttemptRepository answerAttemptRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FlashcardRepository flashcardRepository;

    @InjectMocks
    private AnswerAttemptService service;

    private User user;

    private Flashcard flashcard;

    @BeforeEach
    void setUp() {
        user = org.mockito.Mockito.mock(User.class);
        flashcard = org.mockito.Mockito.mock(Flashcard.class);
    }

    @Test
    void shouldCreateAnswerAttempt() {
        AnswerAttemptRequest request = new AnswerAttemptRequest(1L, 10L, true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(flashcardRepository.findById(10L)).thenReturn(Optional.of(flashcard));

        when(user.getId()).thenReturn(1L);
        when(flashcard.getId()).thenReturn(10L);

        when(answerAttemptRepository.save(any(AnswerAttempt.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AnswerAttemptResponse response = service.create(request);

        assertTrue(response.correct());
        assertEquals(1L, response.userId());
        assertEquals(10L, response.flashcardId());
        assertNotNull(response.answeredAt());

        verify(answerAttemptRepository).save(any(AnswerAttempt.class));
    }

    @Test
    void shouldRejectAttemptWhenUserDoesNotExist() {
        AnswerAttemptRequest request = new AnswerAttemptRequest(99L, 10L, true);

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> service.create(request));

        verify(answerAttemptRepository, never()).save(any(AnswerAttempt.class));
    }

    @Test
    void shouldRejectAttemptWhenFlashcardDoesNotExist() {
        AnswerAttemptRequest request = new AnswerAttemptRequest(1L, 99L, false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(flashcardRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> service.create(request));

        verify(answerAttemptRepository, never()).save(any(AnswerAttempt.class));
    }

    @Test
    void shouldReturnUserAttemptHistory() {
        AnswerAttempt first = new AnswerAttempt(user, flashcard, false);
        AnswerAttempt second = new AnswerAttempt(user, flashcard, true);

        when(userRepository.existsById(1L)).thenReturn(true);
        when(user.getId()).thenReturn(1L);
        when(flashcard.getId()).thenReturn(10L);

        when(answerAttemptRepository.findByUserIdOrderByAnsweredAtDescIdDesc(1L))
                .thenReturn(List.of(second, first));

        List<AnswerAttemptResponse> history = service.findByUser(1L);

        assertEquals(2, history.size());
        assertTrue(history.get(0).correct());
        assertFalse(history.get(1).correct());
    }
}