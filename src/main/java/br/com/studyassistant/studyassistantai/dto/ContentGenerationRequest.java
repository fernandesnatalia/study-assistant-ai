package br.com.studyassistant.studyassistantai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ContentGenerationRequest(
        @NotNull Long topicId,
        @NotBlank String request,
        String context,
        SemanticSearchRequest semanticSearch
) {
}