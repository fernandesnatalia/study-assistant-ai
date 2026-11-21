package br.com.studyassistant.studyassistantai.dto;

import java.util.List;

public record RagContext (
    String text,
    List<RetrievedSource> sources
){
}