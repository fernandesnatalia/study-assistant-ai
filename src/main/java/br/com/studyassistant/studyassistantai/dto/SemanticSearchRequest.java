package br.com.studyassistant.studyassistantai.dto;

import br.com.studyassistant.studyassistantai.model.SourceSelectionMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.util.Set;

public record SemanticSearchRequest(
    @NotBlank String query,
    Set<Long> subjectsIds,
    Set<Long> topicsIds,
    Set<Long> contentsIds,
    SourceSelectionMode selectionMode,
    @Positive Integer topK,
    Double similarityThreshold
) {
}