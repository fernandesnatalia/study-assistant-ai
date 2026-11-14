package br.com.studyassistant.studyassistantai.controller;

import br.com.studyassistant.studyassistantai.dto.StudyAssistantRequest;
import br.com.studyassistant.studyassistantai.dto.StudyAssistantResponse;
import br.com.studyassistant.studyassistantai.service.StudyAssistantService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/study-assistant")
public class StudyAssistantController {

    private final StudyAssistantService studyAssistantService;

    public StudyAssistantController(StudyAssistantService studyAssistantService) {
        this.studyAssistantService = studyAssistantService;
    }

    @PostMapping("/ask")
    public StudyAssistantResponse ask(
            @Valid @RequestBody StudyAssistantRequest request
    ) {
        return studyAssistantService.ask(request);
    }
}