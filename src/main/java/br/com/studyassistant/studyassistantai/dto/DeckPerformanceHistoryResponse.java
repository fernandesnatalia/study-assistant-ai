package br.com.studyassistant.studyassistantai.dto;

public record DeckPerformanceHistoryResponse(
        Long id,
        Long originalDeckId,
        String theme,
        long totalAttempts,
        long correctAnswers,
        long incorrectAnswers,
        double accuracyPercentage
) {
}