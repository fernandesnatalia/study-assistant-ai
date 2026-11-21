package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.dto.AnswerAttemptResponse;
import br.com.studyassistant.studyassistantai.model.AnswerAttempt;
import br.com.studyassistant.studyassistantai.enums.AnswerAttemptResult;
import br.com.studyassistant.studyassistantai.model.Deck;
import br.com.studyassistant.studyassistantai.model.Flashcard;
import br.com.studyassistant.studyassistantai.model.User;
import br.com.studyassistant.studyassistantai.enums.DeckMode;
import br.com.studyassistant.studyassistantai.repository.AnswerAttemptRepository;
import br.com.studyassistant.studyassistantai.repository.FlashcardRepository;
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

    @Mock
    private AnswerEvaluationAiService answerEvaluationAiService;

    @InjectMocks
    private AnswerAttemptService service;

    private User user;

    private Deck deck;

    private Flashcard flashcard;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        deck = new Deck();
        deck.setId(1L);
        deck.setUser(user);
        deck.setTheme("Algoritmos");
        deck.setMode(DeckMode.TRAINING);

        flashcard = new Flashcard();
        flashcard.setId(10L);
        flashcard.setQuestion("O que é um algoritmo?");
        flashcard.setAnswer("É uma sequência finita de instruções.");
        flashcard.setDeck(deck);
    }

    @Test
    void shouldRegisterCorrectAnswerEvaluatedByAi() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(flashcardRepository.findByIdAndDeckUserId(10L, 1L)).thenReturn(Optional.of(flashcard));

        when(answerEvaluationAiService.evaluate(
                flashcard.getQuestion(),
                flashcard.getAnswer(),
                "É uma sequência de passos para resolver um problema."
        )).thenReturn(AnswerAttemptResult.CORRECT);

        when(answerAttemptRepository.save(any(AnswerAttempt.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AnswerAttemptResponse response = service.answer(
                1L,
                10L,
                "É uma sequência de passos para resolver um problema."
        );

        assertEquals(1L, response.userId());
        assertEquals(10L, response.flashcardId());
        assertEquals(
                "É uma sequência de passos para resolver um problema.",
                response.userAnswer()
        );
        assertEquals(AnswerAttemptResult.CORRECT, response.result());
        assertEquals("É uma sequência finita de instruções.", response.expectedAnswer());

        verify(answerEvaluationAiService).evaluate(
                flashcard.getQuestion(),
                flashcard.getAnswer(),
                "É uma sequência de passos para resolver um problema."
        );
        verify(answerAttemptRepository).save(any(AnswerAttempt.class));
    }

    @Test
    void shouldRegisterIncorrectAnswerEvaluatedByAi() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(flashcardRepository.findByIdAndDeckUserId(10L, 1L)).thenReturn(Optional.of(flashcard));

        when(answerEvaluationAiService.evaluate(
                flashcard.getQuestion(),
                flashcard.getAnswer(),
                "É um tipo de banco de dados."
        )).thenReturn(AnswerAttemptResult.INCORRECT);

        when(answerAttemptRepository.save(any(AnswerAttempt.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AnswerAttemptResponse response = service.answer(
                1L,
                10L,
                "É um tipo de banco de dados."
        );

        assertEquals("É um tipo de banco de dados.", response.userAnswer());
        assertEquals(AnswerAttemptResult.INCORRECT, response.result());

        verify(answerAttemptRepository).save(any(AnswerAttempt.class));
    }

    @Test
    void shouldRejectAnswerWhenDeckIsNotInTraining() {
        deck.setMode(DeckMode.STUDY);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(flashcardRepository.findByIdAndDeckUserId(10L, 1L)).thenReturn(Optional.of(flashcard));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.answer(
                        1L,
                        10L,
                        "É uma sequência de passos para resolver um problema."
                )
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(answerEvaluationAiService, never()).evaluate(any(), any(), any());
        verify(answerAttemptRepository, never()).save(any(AnswerAttempt.class));
    }

    @Test
    void shouldRejectAnswerWhenFlashcardDoesNotHaveExpectedAnswer() {
        flashcard.setAnswer(null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(flashcardRepository.findByIdAndDeckUserId(10L, 1L)).thenReturn(Optional.of(flashcard));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> service.answer(1L, 10L, "Minha resposta")
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(answerEvaluationAiService, never()).evaluate(any(), any(), any());
        verify(answerAttemptRepository, never()).save(any(AnswerAttempt.class));
    }

    @Test
    void shouldRegisterGiveUpAsIncorrectAttempt() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(flashcardRepository.findByIdAndDeckUserId(10L, 1L)).thenReturn(Optional.of(flashcard));

        when(answerAttemptRepository.save(any(AnswerAttempt.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AnswerAttemptResponse response = service.giveUp(1L, 10L);

        assertEquals(1L, response.userId());
        assertEquals(10L, response.flashcardId());
        assertNull(response.userAnswer());
        assertEquals(AnswerAttemptResult.INCORRECT, response.result());
        assertEquals("É uma sequência finita de instruções.", response.expectedAnswer());

        verify(answerAttemptRepository).save(any(AnswerAttempt.class));
    }

    @Test
    void shouldRejectGiveUpWhenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> service.giveUp(99L, 10L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(answerAttemptRepository, never()).save(any(AnswerAttempt.class));
    }

    @Test
    void shouldRejectGiveUpWhenFlashcardDoesNotExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(flashcardRepository.findByIdAndDeckUserId(99L, 1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> service.giveUp(1L, 99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(answerAttemptRepository, never()).save(any(AnswerAttempt.class));
    }

    @Test
    void shouldRejectGiveUpForAnotherUsersFlashcard() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(flashcardRepository.findByIdAndDeckUserId(10L, 2L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> service.giveUp(2L, 10L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(answerAttemptRepository, never()).save(any(AnswerAttempt.class));
    }

    @Test
    void shouldRejectGiveUpWhenDeckIsNotInTraining() {
        deck.setMode(DeckMode.STUDY);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(flashcardRepository.findByIdAndDeckUserId(10L, 1L)).thenReturn(Optional.of(flashcard));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> service.giveUp(1L, 10L)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(answerAttemptRepository, never()).save(any(AnswerAttempt.class));
    }

    @Test
    void shouldReturnUserAttemptHistory() {
        AnswerAttempt attempt = new AnswerAttempt(user, flashcard, null, AnswerAttemptResult.INCORRECT);

        when(userRepository.existsById(1L)).thenReturn(true);
        when(answerAttemptRepository.findByUserIdOrderByAttemptedAtDescIdDesc(1L)).thenReturn(List.of(attempt));

        List<AnswerAttemptResponse> history = service.findByUser(1L);

        assertEquals(1, history.size());
        assertEquals(AnswerAttemptResult.INCORRECT, history.get(0).result());
        assertNull(history.get(0).userAnswer());
    }

    @Test
    void shouldReturnUserAttemptHistoryForFlashcard() {
        AnswerAttempt attempt = new AnswerAttempt(user, flashcard, null, AnswerAttemptResult.INCORRECT);

        when(userRepository.existsById(1L)).thenReturn(true);
        when(flashcardRepository.findByIdAndDeckUserId(10L, 1L)).thenReturn(Optional.of(flashcard));

        when(answerAttemptRepository
                .findByUserIdAndFlashcardIdOrderByAttemptedAtDescIdDesc(1L, 10L))
                .thenReturn(List.of(attempt));

        List<AnswerAttemptResponse> history = service.findByUserAndFlashcard(1L, 10L);

        assertEquals(1, history.size());
        assertEquals(AnswerAttemptResult.INCORRECT, history.get(0).result());

        verify(answerAttemptRepository).findByUserIdAndFlashcardIdOrderByAttemptedAtDescIdDesc(1L, 10L);
    }

    @Test
    void shouldRejectHistoryAccessForAnotherUsersFlashcard() {
        when(userRepository.existsById(2L)).thenReturn(true);
        when(flashcardRepository.findByIdAndDeckUserId(10L, 2L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> service.findByUserAndFlashcard(2L, 10L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(answerAttemptRepository, never())
                .findByUserIdAndFlashcardIdOrderByAttemptedAtDescIdDesc(2L, 10L);
    }

    @Test
    void shouldRejectHistoryAccessWhenUserDoesNotExist() {
        when(userRepository.existsById(99L)).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> service.findByUser(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(answerAttemptRepository, never()).findByUserIdOrderByAttemptedAtDescIdDesc(99L);
    }

    @Test
    void shouldReturnEmptyHistoryWhenUserHasNoAttempts() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(answerAttemptRepository.findByUserIdOrderByAttemptedAtDescIdDesc(1L)).thenReturn(List.of());

        List<AnswerAttemptResponse> history = service.findByUser(1L);

        assertTrue(history.isEmpty());

        verify(answerAttemptRepository).findByUserIdOrderByAttemptedAtDescIdDesc(1L);
    }
}