package br.com.studyassistant.studyassistantai.dto;

import jakarta.validation.constraints.NotNull;

public record AnswerAttemptRequest(
        @NotNull Long userId,
        @NotNull Long flashcardId,
        @NotNull Boolean correct
) {
}