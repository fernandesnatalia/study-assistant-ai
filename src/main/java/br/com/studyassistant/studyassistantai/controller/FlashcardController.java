package br.com.studyassistant.studyassistantai.controller;

import br.com.studyassistant.studyassistantai.dto.FlashcardGenerationRequest;
import br.com.studyassistant.studyassistantai.model.Flashcard;
import br.com.studyassistant.studyassistantai.service.AuthenticatedUserService;
import br.com.studyassistant.studyassistantai.service.FlashcardService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
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
    private final AuthenticatedUserService authenticatedUserService;

    public FlashcardController(
            FlashcardService flashcardService,
            AuthenticatedUserService authenticatedUserService
    ) {
        this.flashcardService = flashcardService;
        this.authenticatedUserService = authenticatedUserService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Flashcard createFlashcard(
            @RequestParam Long deckId,
            @Valid @RequestBody Flashcard flashcard,
            Authentication authentication
    ) {
        Long userId = authenticatedUserService.getAuthenticatedUserId(authentication);
        return flashcardService.createFlashcard(userId, deckId, flashcard);
    }

    @PostMapping("/generate")
    @ResponseStatus(HttpStatus.CREATED)
    public List<Flashcard> generateFlashcards(
            @RequestParam Long deckId,
            @Valid @RequestBody FlashcardGenerationRequest request,
            Authentication authentication
    ) {
        Long userId = authenticatedUserService.getAuthenticatedUserId(authentication);

        return flashcardService.generateFlashcards(
                userId,
                deckId,
                request.quantity(),
                request.contentId(),
                request.topicId(),
                request.context()
        );
    }

    @GetMapping
    public List<Flashcard> getAllFlashcards(Authentication authentication) {
        Long userId = authenticatedUserService.getAuthenticatedUserId(authentication);
        return flashcardService.getAllFlashcards(userId);
    }

    @GetMapping("/{id}")
    public Flashcard getFlashcardById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = authenticatedUserService.getAuthenticatedUserId(authentication);
        return flashcardService.getFlashcardById(id, userId);
    }

    @GetMapping("/deck/{deckId}")
    public List<Flashcard> getFlashcardsByDeckId(
            @PathVariable Long deckId,
            Authentication authentication
    ) {
        Long userId = authenticatedUserService.getAuthenticatedUserId(authentication);
        return flashcardService.getFlashcardsByDeckId(deckId, userId);
    }

    @PutMapping("/{id}")
    public Flashcard updateFlashcard(
            @PathVariable Long id,
            @Valid @RequestBody Flashcard updatedFlashcard,
            Authentication authentication
    ) {
        Long userId = authenticatedUserService.getAuthenticatedUserId(authentication);
        return flashcardService.updateFlashcard(id, userId, updatedFlashcard);
    }

    @PostMapping("/{id}/generate-answer")
    public Flashcard generateAnswer(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = authenticatedUserService.getAuthenticatedUserId(authentication);
        return flashcardService.generateFlashcardAnswer(id, userId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFlashcard(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = authenticatedUserService.getAuthenticatedUserId(authentication);
        flashcardService.deleteFlashcard(id, userId);
    }
}