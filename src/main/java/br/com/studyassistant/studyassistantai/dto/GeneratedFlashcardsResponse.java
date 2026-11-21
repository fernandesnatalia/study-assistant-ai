package br.com.studyassistant.studyassistantai.dto;

import br.com.studyassistant.studyassistantai.model.Flashcard;
import java.util.List;

public record GeneratedFlashcardsResponse(
        List<Flashcard> flashcards,
        List<RetrievedSource> sources
) {
}