package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.dto.DeckPerformanceResponse;
import br.com.studyassistant.studyassistantai.model.AnswerAttempt;
import br.com.studyassistant.studyassistantai.enums.AnswerAttemptResult;
import br.com.studyassistant.studyassistantai.model.Deck;
import br.com.studyassistant.studyassistantai.model.Flashcard;
import br.com.studyassistant.studyassistantai.model.User;
import br.com.studyassistant.studyassistantai.enums.DeckMode;
import br.com.studyassistant.studyassistantai.model.DeckPerformanceHistory;
import br.com.studyassistant.studyassistantai.repository.DeckPerformanceHistoryRepository;
import br.com.studyassistant.studyassistantai.repository.AnswerAttemptRepository;
import br.com.studyassistant.studyassistantai.repository.DeckRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PerformanceServiceTest {

    @Mock
    private AnswerAttemptRepository answerAttemptRepository;

    @Mock
    private DeckRepository deckRepository;

    @Mock
    private DeckPerformanceHistoryRepository deckPerformanceHistoryRepository;

    @InjectMocks
    private PerformanceService service;

    private User user;

    private Deck deck;

    private Flashcard flashcard;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        deck = new Deck();
        deck.setId(1L);
        deck.setTheme("Teoria dos Anéis");
        deck.setUser(user);
        deck.setMode(DeckMode.TRAINING);

        flashcard = new Flashcard();
        flashcard.setId(10L);
        flashcard.setQuestion("O que é um anel comutativo?");
        flashcard.setAnswer("É um anel cuja multiplicação é comutativa.");
        flashcard.setDeck(deck);
    }

    @Test
    void shouldCalculateDeckPerformance() {
        AnswerAttempt correctAttempt = new AnswerAttempt(
                user, flashcard, "Um anel com multiplicação comutativa.", AnswerAttemptResult.CORRECT
        );

        AnswerAttempt incorrectAttempt = new AnswerAttempt(
                user, flashcard, "Um grupo comutativo.", AnswerAttemptResult.INCORRECT
        );

        when(deckRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(deck));

        when(answerAttemptRepository.findByUserIdAndFlashcardDeckId(1L, 1L))
                .thenReturn(List.of(correctAttempt, incorrectAttempt));

        DeckPerformanceResponse response = service.getDeckPerformance(1L, 1L);

        assertEquals(2, response.totalAttempts());
        assertEquals(1, response.correctAnswers());
        assertEquals(1, response.incorrectAnswers());
        assertEquals(50.0, response.accuracyPercentage());
    }

    @Test
    void shouldReturnZeroPerformanceWhenDeckHasNoAttempts() {
        when(deckRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(deck));
        when(answerAttemptRepository.findByUserIdAndFlashcardDeckId(1L, 1L)).thenReturn(List.of());

        DeckPerformanceResponse response = service.getDeckPerformance(1L, 1L);

        assertEquals(0, response.totalAttempts());
        assertEquals(0, response.correctAnswers());
        assertEquals(0, response.incorrectAnswers());
        assertEquals(0.0, response.accuracyPercentage());
    }

    @Test
    void shouldRejectPerformanceForAnotherUsersDeck() {
        when(deckRepository.findByIdAndUserId(1L, 2L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> service.getDeckPerformance(2L, 1L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void shouldRejectPerformanceWhenDeckIsNotInTraining() {
        deck.setMode(DeckMode.STUDY);

        when(deckRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(deck));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> service.getDeckPerformance(1L, 1L));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    void shouldConsolidatePerformanceAndDeleteAttempts() {
        AnswerAttempt correctAttempt = new AnswerAttempt(
                user, flashcard, "Resposta correta", AnswerAttemptResult.CORRECT
        );

        AnswerAttempt incorrectAttempt = new AnswerAttempt(
                user, flashcard, "Resposta incorreta", AnswerAttemptResult.INCORRECT
        );

        when(deckRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(deck));

        when(deckPerformanceHistoryRepository
                .findByOriginalDeckIdAndUserId(1L, 1L))
                .thenReturn(Optional.empty());

        when(answerAttemptRepository.findByUserIdAndFlashcardDeckId(1L, 1L))
                .thenReturn(List.of(correctAttempt, incorrectAttempt));

        when(deckPerformanceHistoryRepository.save(any(DeckPerformanceHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DeckPerformanceHistory history = service.consolidateDeckPerformance(1L, 1L);

        assertEquals(2, history.getTotalAttempts());
        assertEquals(1, history.getCorrectAnswers());
        assertEquals(1, history.getIncorrectAnswers());
        assertEquals(50.0, history.getAccuracyPercentage());

        verify(deckPerformanceHistoryRepository).save(any(DeckPerformanceHistory.class));
        verify(answerAttemptRepository).deleteByFlashcardDeckId(1L);
    }
}