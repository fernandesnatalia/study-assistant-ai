package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.dto.DeckPerformanceResponse;
import br.com.studyassistant.studyassistantai.model.AnswerAttempt;
import br.com.studyassistant.studyassistantai.model.AnswerAttemptResult;
import br.com.studyassistant.studyassistantai.model.Deck;
import br.com.studyassistant.studyassistantai.model.DeckMode;
import br.com.studyassistant.studyassistantai.model.DeckPerformanceHistory;
import br.com.studyassistant.studyassistantai.repository.AnswerAttemptRepository;
import br.com.studyassistant.studyassistantai.repository.DeckPerformanceHistoryRepository;
import br.com.studyassistant.studyassistantai.repository.DeckRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class PerformanceService {

    private final AnswerAttemptRepository answerAttemptRepository;
    private final DeckRepository deckRepository;
    private final DeckPerformanceHistoryRepository deckPerformanceHistoryRepository;

    public PerformanceService(
            AnswerAttemptRepository answerAttemptRepository,
            DeckRepository deckRepository,
            DeckPerformanceHistoryRepository deckPerformanceHistoryRepository
    ) {
        this.answerAttemptRepository = answerAttemptRepository;
        this.deckRepository = deckRepository;
        this.deckPerformanceHistoryRepository = deckPerformanceHistoryRepository;
    }

    @Transactional(readOnly = true)
    public DeckPerformanceResponse getDeckPerformance(Long userId, Long deckId) {
        Deck deck = getTrainingDeck(userId, deckId);

        List<AnswerAttempt> attempts = answerAttemptRepository.findByUserIdAndFlashcardDeckId(userId, deckId);

        return calculatePerformance(deck, attempts);
    }

    @Transactional
    public DeckPerformanceHistory consolidateDeckPerformance(Long userId, Long deckId
    ) {
        Deck deck = getTrainingDeck(userId, deckId);

        if (deckPerformanceHistoryRepository
                .findByOriginalDeckIdAndUserId(deckId, userId)
                .isPresent()
        ) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Deck performance has already been consolidated");
        }

        List<AnswerAttempt> attempts = answerAttemptRepository.findByUserIdAndFlashcardDeckId(userId, deckId);

        DeckPerformanceResponse performance = calculatePerformance(deck, attempts);

        DeckPerformanceHistory history = new DeckPerformanceHistory(
                deck.getUser(),
                deck.getId(),
                deck.getTheme(),
                performance.totalAttempts(),
                performance.correctAnswers(),
                performance.incorrectAnswers(),
                performance.accuracyPercentage()
        );

        DeckPerformanceHistory savedHistory = deckPerformanceHistoryRepository.save(history);

        answerAttemptRepository.deleteByFlashcardDeckId(deckId);

        return savedHistory;
    }

    private DeckPerformanceResponse calculatePerformance(
            Deck deck,
            List<AnswerAttempt> attempts
    ) {
        long totalAttempts = attempts.size();

        long correctAnswers = attempts.stream()
                .filter(attempt -> attempt.getResult() == AnswerAttemptResult.CORRECT)
                .count();

        long incorrectAnswers = attempts.stream()
                .filter(attempt -> attempt.getResult() == AnswerAttemptResult.INCORRECT)
                .count();

        double accuracyPercentage = totalAttempts == 0 ? 0.0 : (correctAnswers * 100.0) / totalAttempts;

        return new DeckPerformanceResponse(
                deck.getId(),
                deck.getTheme(),
                totalAttempts,
                correctAnswers,
                incorrectAnswers,
                accuracyPercentage
        );
    }

    private Deck getTrainingDeck(Long userId, Long deckId) {
        Deck deck = deckRepository.findByIdAndUserId(deckId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Deck not found"));

        if (deck.getMode() != DeckMode.TRAINING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Deck is not in TRAINING mode");
        }
        return deck;
    }
}