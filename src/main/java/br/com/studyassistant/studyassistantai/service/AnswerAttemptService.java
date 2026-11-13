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

    public AnswerAttemptService(
            AnswerAttemptRepository answerAttemptRepository,
            UserRepository userRepository,
            FlashcardRepository flashcardRepository
    ) {
        this.answerAttemptRepository = answerAttemptRepository;
        this.userRepository = userRepository;
        this.flashcardRepository = flashcardRepository;
    }

    @Transactional
    public AnswerAttemptResponse giveUp(Long userId, Long flashcardId) {
        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Flashcard flashcard = flashcardRepository
                .findByIdAndDeckUserId(flashcardId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Flashcard not found"));

        if (!flashcard.getDeck().isTraining()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Deck is not in training");
        }

        AnswerAttempt attempt = new AnswerAttempt(user, flashcard, null, AnswerAttemptResult.INCORRECT);
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

        flashcardRepository.findByIdAndDeckUserId(flashcardId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Flashcard not found"));

        return answerAttemptRepository
                .findByUserIdAndFlashcardIdOrderByAttemptedAtDescIdDesc(userId, flashcardId)
                .stream()
                .map(this::toResponse)
                .toList();
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