package br.com.studyassistant.studyassistantai.dto;

import br.com.studyassistant.studyassistantai.enums.AnswerAttemptResult;
import java.time.LocalDateTime;

public record AnswerAttemptResponse(
        Long id,
        Long userId,
        Long flashcardId,
        String userAnswer,
        AnswerAttemptResult result,
        String expectedAnswer,
        LocalDateTime attemptedAt
) {
}