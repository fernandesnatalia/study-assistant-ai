package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.dto.ReviewFlashcardResponse;
import br.com.studyassistant.studyassistantai.model.AnswerAttempt;
import br.com.studyassistant.studyassistantai.model.AnswerAttemptResult;
import br.com.studyassistant.studyassistantai.model.DeckMode;
import br.com.studyassistant.studyassistantai.model.Flashcard;
import br.com.studyassistant.studyassistantai.repository.AnswerAttemptRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReviewService {

    private final AnswerAttemptRepository answerAttemptRepository;

    public ReviewService(AnswerAttemptRepository answerAttemptRepository) {
        this.answerAttemptRepository = answerAttemptRepository;
    }

    @Transactional(readOnly = true)
    public List<ReviewFlashcardResponse> findPendingReviews(Long userId) {
        List<AnswerAttempt> attempts =
                answerAttemptRepository.findByUserIdAndFlashcardDeckModeOrderByAttemptedAtDescIdDesc(
                        userId,
                        DeckMode.TRAINING
                );

        Map<Long, AnswerAttempt> latestAttempts = new LinkedHashMap<>();

        for (AnswerAttempt attempt : attempts) {
            latestAttempts.putIfAbsent(attempt.getFlashcard().getId(), attempt);
        }

        return latestAttempts.values()
                .stream()
                .filter(attempt -> attempt.getResult() == AnswerAttemptResult.INCORRECT)
                .map(this::toResponse)
                .toList();
    }

    private ReviewFlashcardResponse toResponse(AnswerAttempt attempt) {
        Flashcard flashcard = attempt.getFlashcard();

        return new ReviewFlashcardResponse(
                flashcard.getId(),
                flashcard.getDeck().getId(),
                flashcard.getDeck().getTheme(),
                flashcard.getQuestion()
        );
    }
}