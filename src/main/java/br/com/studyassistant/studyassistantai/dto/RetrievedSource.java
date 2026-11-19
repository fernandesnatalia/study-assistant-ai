package br.com.studyassistant.studyassistantai.dto;

public record RetrievedSource(
        String chunkId,
        Integer chunkIndex,
        String text,
        Double similarity,
        Long contentId,
        String contentTitle,
        Long topicId,
        String topicName,
        Long subjectId,
        String subjectName,
        Long documentId,
        String documentName
) {
}