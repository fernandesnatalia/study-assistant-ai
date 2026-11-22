package br.com.studyassistant.studyassistantai.controller;

import br.com.studyassistant.studyassistantai.dto.DeckPerformanceHistoryResponse;
import br.com.studyassistant.studyassistantai.service.AuthenticatedUserService;
import br.com.studyassistant.studyassistantai.service.DeckPerformanceHistoryService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/performance/history")
public class DeckPerformanceHistoryController {

    private final DeckPerformanceHistoryService historyService;
    private final AuthenticatedUserService authenticatedUserService;

    public DeckPerformanceHistoryController(
            DeckPerformanceHistoryService historyService,
            AuthenticatedUserService authenticatedUserService
    ) {
        this.historyService = historyService;
        this.authenticatedUserService = authenticatedUserService;
    }

    @GetMapping
    public List<DeckPerformanceHistoryResponse> findAll(
            Authentication authentication
    ) {
        Long userId = authenticatedUserService.getAuthenticatedUserId(authentication);

        return historyService.findAll(userId);
    }

    @GetMapping("/{id}")
    public DeckPerformanceHistoryResponse findById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = authenticatedUserService.getAuthenticatedUserId(authentication);

        return historyService.findById(id, userId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = authenticatedUserService.getAuthenticatedUserId(authentication);

        historyService.delete(id, userId);
    }
}