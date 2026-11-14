package br.com.studyassistant.studyassistantai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ContentApprovalRequest(
        @NotNull Long topicId,
        @NotBlank String title,
        @NotBlank String body
) {
}