package br.com.studyassistant.studyassistantai.dto;

public record DocumentPreviewResponse(
        String originalFilename,
        String format,
        Long sizeBytes,
        String title,
        String body,
        Integer extractedCharacterCount
) {
}