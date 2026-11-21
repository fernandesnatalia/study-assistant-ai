package br.com.studyassistant.studyassistantai.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record FlashcardGenerationRequest(
        @NotNull
        @Min(1)
        @Max(10)
        Integer quantity,
        Long contentId,
        Long topicId,
        String context,
        SemanticSearchRequest semanticSearch
) {
}