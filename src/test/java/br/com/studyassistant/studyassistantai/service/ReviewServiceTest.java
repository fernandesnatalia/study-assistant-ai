package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.dto.ReviewFlashcardResponse;
import br.com.studyassistant.studyassistantai.model.AnswerAttempt;
import br.com.studyassistant.studyassistantai.model.AnswerAttemptResult;
import br.com.studyassistant.studyassistantai.model.Deck;
import br.com.studyassistant.studyassistantai.model.Flashcard;
import br.com.studyassistant.studyassistantai.model.User;
import br.com.studyassistant.studyassistantai.repository.AnswerAttemptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private AnswerAttemptRepository answerAttemptRepository;

    @InjectMocks
    private ReviewService service;

    private User user;

    private Flashcard flashcard;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        Deck deck = new Deck();
        deck.setId(1L);
        deck.setTheme("Teoria dos Anéis");
        deck.setUser(user);
        deck.setTraining(true);

        flashcard = new Flashcard();
        flashcard.setId(10L);
        flashcard.setQuestion("O que é um anel comutativo?");
        flashcard.setAnswer("É um anel cuja multiplicação é comutativa.");
        flashcard.setDeck(deck);
    }

    @Test
    void shouldReturnFlashcardWhenLatestAttemptIsIncorrect() {
        AnswerAttempt attempt = new AnswerAttempt(
                user, flashcard, "É um grupo comutativo.", AnswerAttemptResult.INCORRECT
        );

        when(answerAttemptRepository
                .findByUserIdAndFlashcardDeckTrainingTrueOrderByAttemptedAtDescIdDesc(1L))
                .thenReturn(List.of(attempt));

        List<ReviewFlashcardResponse> result = service.findPendingReviews(1L);

        assertEquals(1, result.size());
        assertEquals(10L, result.getFirst().flashcardId());
        assertEquals(1L, result.getFirst().deckId());
        assertEquals("Teoria dos Anéis", result.getFirst().deckTheme());
    }

    @Test
    void shouldNotReturnFlashcardWhenLatestAttemptIsCorrect() {
        AnswerAttempt attempt = new AnswerAttempt(
                user, flashcard, "É um anel cuja multiplicação é comutativa.", AnswerAttemptResult.CORRECT
        );

        when(answerAttemptRepository
                .findByUserIdAndFlashcardDeckTrainingTrueOrderByAttemptedAtDescIdDesc(1L))
                .thenReturn(List.of(attempt));

        List<ReviewFlashcardResponse> result = service.findPendingReviews(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldUseOnlyLatestAttemptForEachFlashcard() {
        AnswerAttempt latestAttempt = new AnswerAttempt(
                user, flashcard, "É um anel cuja multiplicação é comutativa.", AnswerAttemptResult.CORRECT
        );

        AnswerAttempt olderAttempt = new AnswerAttempt(
                user, flashcard, "É um grupo comutativo.", AnswerAttemptResult.INCORRECT
        );

        when(answerAttemptRepository
                .findByUserIdAndFlashcardDeckTrainingTrueOrderByAttemptedAtDescIdDesc(1L))
                .thenReturn(List.of(latestAttempt, olderAttempt));

        List<ReviewFlashcardResponse> result = service.findPendingReviews(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoPendingReviews() {
        when(answerAttemptRepository
                .findByUserIdAndFlashcardDeckTrainingTrueOrderByAttemptedAtDescIdDesc(1L))
                .thenReturn(List.of());

        List<ReviewFlashcardResponse> result = service.findPendingReviews(1L);

        assertTrue(result.isEmpty());
    }
}