package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.model.User;
import br.com.studyassistant.studyassistantai.model.Deck;
import br.com.studyassistant.studyassistantai.model.Flashcard;
import br.com.studyassistant.studyassistantai.model.AnswerAttempt;
import br.com.studyassistant.studyassistantai.model.DeckPerformanceHistory;
import br.com.studyassistant.studyassistantai.enums.DeckMode;
import br.com.studyassistant.studyassistantai.enums.AnswerAttemptResult;
import br.com.studyassistant.studyassistantai.repository.UserRepository;
import br.com.studyassistant.studyassistantai.repository.DeckRepository;
import br.com.studyassistant.studyassistantai.repository.FlashcardRepository;
import br.com.studyassistant.studyassistantai.repository.AnswerAttemptRepository;
import br.com.studyassistant.studyassistantai.repository.DeckPerformanceHistoryRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class PerformanceServiceIntegrationTest {

    @Autowired
    private PerformanceService performanceService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DeckRepository deckRepository;

    @Autowired
    private FlashcardRepository flashcardRepository;

    @Autowired
    private AnswerAttemptRepository answerAttemptRepository;

    @Autowired
    private DeckPerformanceHistoryRepository historyRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldPersistHistoryAndDeleteAttempts() {
        User user = new User();
        user.setName("Integration Test");
        user.setEmail("performance-test-" + java.util.UUID.randomUUID() + "@example.com");
        user.setPassword("test-password");
        user = userRepository.save(user);

        Deck deck = new Deck();
        deck.setTheme("Algoritmos");
        deck.setMode(DeckMode.TRAINING);
        deck.setUser(user);
        deck = deckRepository.save(deck);

        Flashcard flashcard = new Flashcard();
        flashcard.setQuestion("O que é um algoritmo?");
        flashcard.setAnswer("Uma sequência de instruções.");
        flashcard.setDeck(deck);
        flashcard = flashcardRepository.save(flashcard);

        answerAttemptRepository.save(
                new AnswerAttempt(user, flashcard, "Uma sequência de instruções.", AnswerAttemptResult.CORRECT)
        );

        answerAttemptRepository.save(
                new AnswerAttempt(user, flashcard, "Um tipo de banco de dados.", AnswerAttemptResult.INCORRECT)
        );

        entityManager.flush();
        entityManager.clear();

        Long userId = user.getId();
        Long deckId = deck.getId();

        DeckPerformanceHistory history = performanceService.consolidateDeckPerformance(userId, deckId);

        entityManager.flush();
        entityManager.clear();

        DeckPerformanceHistory persistedHistory = historyRepository.findById(history.getId()).orElseThrow();

        assertEquals(deckId, persistedHistory.getOriginalDeckId());
        assertEquals("Algoritmos", persistedHistory.getTheme());
        assertEquals(2, persistedHistory.getTotalAttempts());
        assertEquals(1, persistedHistory.getCorrectAnswers());
        assertEquals(1, persistedHistory.getIncorrectAnswers());
        assertEquals(50.0, persistedHistory.getAccuracyPercentage());

        assertTrue(answerAttemptRepository.findByUserIdAndFlashcardDeckId(userId, deckId).isEmpty());
        assertTrue(deckRepository.findById(deckId).isPresent());

        deckRepository.deleteById(deckId);

        entityManager.flush();
        entityManager.clear();

        assertTrue(deckRepository.findById(deckId).isEmpty());
        assertTrue(historyRepository.findById(history.getId()).isPresent());
    }
}
