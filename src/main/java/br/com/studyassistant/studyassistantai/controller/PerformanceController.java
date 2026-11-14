package br.com.studyassistant.studyassistantai.controller;

import br.com.studyassistant.studyassistantai.dto.DeckPerformanceResponse;
import br.com.studyassistant.studyassistantai.service.AuthenticatedUserService;
import br.com.studyassistant.studyassistantai.service.PerformanceService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/performance")
public class PerformanceController {

    private final PerformanceService performanceService;
    private final AuthenticatedUserService authenticatedUserService;

    public PerformanceController(
            PerformanceService performanceService,
            AuthenticatedUserService authenticatedUserService
    ) {
        this.performanceService = performanceService;
        this.authenticatedUserService = authenticatedUserService;
    }

    @GetMapping("/decks/{deckId}")
    public DeckPerformanceResponse getDeckPerformance(
            @PathVariable Long deckId,
            Authentication authentication
    ) {
        Long userId = authenticatedUserService.getAuthenticatedUserId(authentication);

        return performanceService.getDeckPerformance(userId, deckId);
    }
}