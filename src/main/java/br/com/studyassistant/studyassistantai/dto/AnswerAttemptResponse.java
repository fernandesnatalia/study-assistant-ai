package br.com.studyassistant.studyassistantai.dto;

import java.time.LocalDateTime;

public record AnswerAttemptResponse(
        Long id,
        Long userId,
        Long flashcardId,
        boolean correct,
        LocalDateTime answeredAt
) {
}