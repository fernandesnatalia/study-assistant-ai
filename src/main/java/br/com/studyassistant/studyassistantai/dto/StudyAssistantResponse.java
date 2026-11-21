package br.com.studyassistant.studyassistantai.dto;

import java.util.List;

public record StudyAssistantResponse(
        String response,
        List<RetrievedSource> sources
) {
}