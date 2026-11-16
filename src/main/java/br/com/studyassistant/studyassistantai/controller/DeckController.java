package br.com.studyassistant.studyassistantai.controller;

import br.com.studyassistant.studyassistantai.dto.DeckAiCreateRequest;
import br.com.studyassistant.studyassistantai.dto.DeckCreateRequest;
import br.com.studyassistant.studyassistantai.model.Deck;
import br.com.studyassistant.studyassistantai.service.AuthenticatedUserService;
import br.com.studyassistant.studyassistantai.service.DeckService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/decks")
public class DeckController {

    private final DeckService deckService;
    private final AuthenticatedUserService authenticatedUserService;

    public DeckController(
            DeckService deckService,
            AuthenticatedUserService authenticatedUserService
    ) {
        this.deckService = deckService;
        this.authenticatedUserService = authenticatedUserService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Deck create(
            @Valid @RequestBody DeckCreateRequest request,
            Authentication authentication
    ) {
        Long userId = authenticatedUserService.getAuthenticatedUserId(authentication);
        return deckService.create(request, userId);
    }

    @PostMapping("/generate")
    @ResponseStatus(HttpStatus.CREATED)
    public Deck createWithAi(
            @Valid @RequestBody DeckAiCreateRequest request,
            Authentication authentication
    ) {
        Long userId = authenticatedUserService.getAuthenticatedUserId(authentication);
        return deckService.createWithAi(request, userId);
    }

    @PostMapping("/{id}/convert-to-study")
    public Deck convertToStudy(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = authenticatedUserService.getAuthenticatedUserId(authentication);
        return deckService.convertToStudy(id, userId);
    }


    @GetMapping
    public List<Deck> findAll(Authentication authentication) {
        Long userId = authenticatedUserService.getAuthenticatedUserId(authentication);
        return deckService.findAll(userId);
    }

    @GetMapping("/{id}")
    public Deck findById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = authenticatedUserService.getAuthenticatedUserId(authentication);
        return deckService.findById(id, userId);
    }

    @PutMapping("/{id}")
    public Deck update(
            @PathVariable Long id,
            @RequestBody Deck deck,
            Authentication authentication
    ) {
        Long userId = authenticatedUserService.getAuthenticatedUserId(authentication);
        return deckService.update(id, deck, userId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = authenticatedUserService.getAuthenticatedUserId(authentication);
        deckService.delete(id, userId);
    }
}