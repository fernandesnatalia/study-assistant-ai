package br.com.studyassistant.studyassistantai.dto;

public record DeckPerformanceResponse(
        Long deckId,
        String theme,
        long totalAttempts,
        long correctAnswers,
        long incorrectAnswers,
        double accuracyPercentage
) {
}