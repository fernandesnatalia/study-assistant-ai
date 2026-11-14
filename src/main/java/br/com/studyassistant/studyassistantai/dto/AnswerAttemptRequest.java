package br.com.studyassistant.studyassistantai.dto;

import jakarta.validation.constraints.NotBlank;

public record AnswerAttemptRequest(
        @NotBlank String answer
) {
}