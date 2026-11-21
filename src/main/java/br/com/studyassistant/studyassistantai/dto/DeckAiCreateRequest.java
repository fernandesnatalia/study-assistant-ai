package br.com.studyassistant.studyassistantai.dto;

import br.com.studyassistant.studyassistantai.enums.DeckMode;
import br.com.studyassistant.studyassistantai.enums.SourceSelectionMode;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Set;

public record DeckAiCreateRequest(
        @NotBlank(message = "Theme is required")
        String theme,

        @NotNull(message = "Mode is required")
        DeckMode mode,

        @NotNull(message = "Quantity is required")
        @Min(1)
        @Max(10)
        Integer quantity,

        Set<Long> subjectsIds,
        Set<Long> topicsIds,
        Set<Long> contentsIds,
        SourceSelectionMode selectionMode
) {
}