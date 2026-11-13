package br.com.studyassistant.studyassistantai.controller;

import br.com.studyassistant.studyassistantai.model.Flashcard;
import br.com.studyassistant.studyassistantai.service.FlashcardService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/flashcards")
public class FlashcardController {

    private final FlashcardService flashcardService;

    public FlashcardController(FlashcardService flashcardService) {
        this.flashcardService = flashcardService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Flashcard createFlashcard(
            @RequestParam Long userId,
            @RequestParam Long deckId,
            @Valid @RequestBody Flashcard flashcard
    ) {
        return flashcardService.createFlashcard(userId, deckId, flashcard);
    }

    @GetMapping
    public List<Flashcard> getAllFlashcards(@RequestParam Long userId) {
        return flashcardService.getAllFlashcards(userId);
    }

    @GetMapping("/{id}")
    public Flashcard getFlashcardById(
            @PathVariable Long id,
            @RequestParam Long userId
    ) {
        return flashcardService.getFlashcardById(id, userId);
    }

    @GetMapping("/deck/{deckId}")
    public List<Flashcard> getFlashcardsByDeckId(
            @PathVariable Long deckId,
            @RequestParam Long userId
    ) {
        return flashcardService.getFlashcardsByDeckId(deckId, userId);
    }

    @PutMapping("/{id}")
    public Flashcard updateFlashcard(
            @PathVariable Long id,
            @RequestParam Long userId,
            @Valid @RequestBody Flashcard updatedFlashcard
    ) {
        return flashcardService.updateFlashcard(id, userId, updatedFlashcard);
    }

    @PostMapping("/{id}/generate-answer")
    public Flashcard generateAnswer(
            @PathVariable Long id,
            @RequestParam Long userId
    ) {
        return flashcardService.generateFlashcardAnswer(id, userId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFlashcard(
            @PathVariable Long id,
            @RequestParam Long userId
    ) {
        flashcardService.deleteFlashcard(id, userId);
    }
}