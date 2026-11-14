package br.com.studyassistant.studyassistantai.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DeckAiCreateRequest(
        @NotBlank(message = "Theme is required")
        String theme,

        @NotNull
        @Min(1)
        @Max(10)
        Integer quantity
) {
}