package br.com.studyassistant.studyassistantai.dto;

public record ReviewFlashcardResponse(
        Long flashcardId,
        Long deckId,
        String deckTheme,
        String question
) {
}