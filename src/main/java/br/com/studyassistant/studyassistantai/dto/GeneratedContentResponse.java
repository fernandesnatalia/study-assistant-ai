package br.com.studyassistant.studyassistantai.dto;

import java.util.List;

public record GeneratedContentResponse(
        Long topicId,
        String title,
        String body,
        List<RetrievedSource> sources
) {
}