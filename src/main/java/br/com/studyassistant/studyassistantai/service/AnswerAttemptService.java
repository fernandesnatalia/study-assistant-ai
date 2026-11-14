package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.dto.AnswerAttemptResponse;
import br.com.studyassistant.studyassistantai.model.AnswerAttempt;
import br.com.studyassistant.studyassistantai.model.AnswerAttemptResult;
import br.com.studyassistant.studyassistantai.model.Flashcard;
import br.com.studyassistant.studyassistantai.model.User;
import br.com.studyassistant.studyassistantai.repository.AnswerAttemptRepository;
import br.com.studyassistant.studyassistantai.repository.FlashcardRepository;
import br.com.studyassistant.studyassistantai.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class AnswerAttemptService {

    private final AnswerAttemptRepository answerAttemptRepository;
    private final UserRepository userRepository;
    private final FlashcardRepository flashcardRepository;
    private final AnswerEvaluationAiService answerEvaluationAiService;

    public AnswerAttemptService(
            AnswerAttemptRepository answerAttemptRepository,
            UserRepository userRepository,
            FlashcardRepository flashcardRepository,
            AnswerEvaluationAiService answerEvaluationAiService
    ) {
        this.answerAttemptRepository = answerAttemptRepository;
        this.userRepository = userRepository;
        this.flashcardRepository = flashcardRepository;
        this.answerEvaluationAiService = answerEvaluationAiService;
    }

    @Transactional
    public AnswerAttemptResponse answer(
            Long userId,
            Long flashcardId,
            String userAnswer
    ) {
        User user = getExistingUser(userId);
        Flashcard flashcard = getExistingFlashcard(flashcardId, userId);

        validateDeckIsInTraining(flashcard);

        if (flashcard.getAnswer() == null || flashcard.getAnswer().isBlank()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Flashcard does not have an expected answer");
        }

        AnswerAttemptResult result = answerEvaluationAiService.evaluate(
                flashcard.getQuestion(),
                flashcard.getAnswer(),
                userAnswer
        );

        AnswerAttempt attempt = new AnswerAttempt(
                user,
                flashcard,
                userAnswer,
                result
        );

        AnswerAttempt saved = answerAttemptRepository.save(attempt);

        return toResponse(saved);
    }

    @Transactional
    public AnswerAttemptResponse giveUp(Long userId, Long flashcardId) {
        User user = getExistingUser(userId);
        Flashcard flashcard = getExistingFlashcard(flashcardId, userId);

        validateDeckIsInTraining(flashcard);

        AnswerAttempt attempt = new AnswerAttempt(
                user,
                flashcard,
                null,
                AnswerAttemptResult.INCORRECT
        );

        AnswerAttempt saved = answerAttemptRepository.save(attempt);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AnswerAttemptResponse> findByUser(Long userId) {
        validateUserExists(userId);

        return answerAttemptRepository
                .findByUserIdOrderByAttemptedAtDescIdDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AnswerAttemptResponse> findByUserAndFlashcard(
            Long userId,
            Long flashcardId
    ) {
        validateUserExists(userId);

        getExistingFlashcard(flashcardId, userId);

        return answerAttemptRepository
                .findByUserIdAndFlashcardIdOrderByAttemptedAtDescIdDesc(userId, flashcardId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private User getExistingUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private Flashcard getExistingFlashcard(Long flashcardId, Long userId) {
        return flashcardRepository.findByIdAndDeckUserId(flashcardId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Flashcard not found"));
    }

    private void validateDeckIsInTraining(Flashcard flashcard) {
        if (!flashcard.getDeck().isTraining()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Deck is not in training");
        }
    }

    private void validateUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
    }

    private AnswerAttemptResponse toResponse(AnswerAttempt attempt) {
        return new AnswerAttemptResponse(
                attempt.getId(),
                attempt.getUser().getId(),
                attempt.getFlashcard().getId(),
                attempt.getUserAnswer(),
                attempt.getResult(),
                attempt.getFlashcard().getAnswer(),
                attempt.getAttemptedAt()
        );
    }
}