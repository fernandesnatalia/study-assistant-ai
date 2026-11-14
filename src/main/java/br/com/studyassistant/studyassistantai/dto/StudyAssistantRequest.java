package br.com.studyassistant.studyassistantai.dto;

import jakarta.validation.constraints.NotBlank;

public record StudyAssistantRequest(

        @NotBlank String request,
        String context
) {
}