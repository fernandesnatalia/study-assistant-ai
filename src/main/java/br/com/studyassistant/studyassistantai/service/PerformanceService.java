package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.dto.DeckPerformanceResponse;
import br.com.studyassistant.studyassistantai.model.AnswerAttempt;
import br.com.studyassistant.studyassistantai.model.AnswerAttemptResult;
import br.com.studyassistant.studyassistantai.model.Deck;
import br.com.studyassistant.studyassistantai.repository.AnswerAttemptRepository;
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

    public PerformanceService(
            AnswerAttemptRepository answerAttemptRepository,
            DeckRepository deckRepository
    ) {
        this.answerAttemptRepository = answerAttemptRepository;
        this.deckRepository = deckRepository;
    }

    @Transactional(readOnly = true)
    public DeckPerformanceResponse getDeckPerformance(Long userId, Long deckId) {
        Deck deck = deckRepository.findByIdAndUserId(deckId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Deck not found"));

        if (!deck.isTraining()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Deck is not in training");
        }

        List<AnswerAttempt> attempts = answerAttemptRepository.findByUserIdAndFlashcardDeckId(userId, deckId);

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
}