package br.com.studyassistant.studyassistantai.controller;

import br.com.studyassistant.studyassistantai.dto.DeckCreateRequest;
import br.com.studyassistant.studyassistantai.model.Deck;
import br.com.studyassistant.studyassistantai.service.DeckService;
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
@RequestMapping("/api/decks")
public class DeckController {

    private final DeckService deckService;

    public DeckController(DeckService deckService) {
        this.deckService = deckService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Deck create(
            @Valid @RequestBody DeckCreateRequest request,
            @RequestParam Long userId
    ) {
        return deckService.create(request, userId);
    }

    @PostMapping("/{id}/training")
    public Deck startTraining(
            @PathVariable Long id,
            @RequestParam Long userId
    ) {
        return deckService.startTraining(id, userId);
    }

    @PostMapping("/{id}/stop-training")
    public Deck stopTraining(
            @PathVariable Long id,
            @RequestParam Long userId
    ) {
        return deckService.stopTraining(id, userId);
    }

    @GetMapping
    public List<Deck> findAll(@RequestParam Long userId) {
        return deckService.findAll(userId);
    }

    @GetMapping("/{id}")
    public Deck findById(
            @PathVariable Long id,
            @RequestParam Long userId
    ) {
        return deckService.findById(id, userId);
    }

    @PutMapping("/{id}")
    public Deck update(
            @PathVariable Long id,
            @RequestBody Deck deck,
            @RequestParam Long userId
    ) {
        return deckService.update(id, deck, userId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long id,
            @RequestParam Long userId
    ) {
        deckService.delete(id, userId);
    }
}