package br.com.studyassistant.studyassistantai.controller;

import br.com.studyassistant.studyassistantai.dto.StudyAssistantRequest;
import br.com.studyassistant.studyassistantai.dto.StudyAssistantResponse;
import br.com.studyassistant.studyassistantai.service.AuthenticatedUserService;
import br.com.studyassistant.studyassistantai.service.StudyAssistantService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/study-assistant")
public class StudyAssistantController {

    private final StudyAssistantService studyAssistantService;
    private final AuthenticatedUserService authenticatedUserService;

    public StudyAssistantController(
            StudyAssistantService studyAssistantService,
            AuthenticatedUserService authenticatedUserService
    ) {
        this.studyAssistantService = studyAssistantService;
        this.authenticatedUserService = authenticatedUserService;
    }

    @PostMapping("/ask")
    public StudyAssistantResponse ask(
            @Valid @RequestBody StudyAssistantRequest request,
            Authentication authentication
    ) {
        Long userId = authenticatedUserService.getAuthenticatedUserId(authentication);
        return studyAssistantService.ask(userId, request);
    }
}