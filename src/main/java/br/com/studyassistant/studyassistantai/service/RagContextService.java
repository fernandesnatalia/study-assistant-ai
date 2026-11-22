package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.dto.RagContext;
import br.com.studyassistant.studyassistantai.dto.RetrievedSource;
import br.com.studyassistant.studyassistantai.dto.SemanticSearchRequest;
import br.com.studyassistant.studyassistantai.enums.SourceSelectionMode;
import br.com.studyassistant.studyassistantai.prompt.RagPromptBuilder;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import static br.com.studyassistant.studyassistantai.constants.ErrorMessages.INSUFFICIENT_SOURCES;

@Service
public class RagContextService {

    private final SemanticRetrievalService semanticRetrievalService;
    private final RagPromptBuilder ragPromptBuilder;

    public RagContextService(
            SemanticRetrievalService semanticRetrievalService,
            RagPromptBuilder ragPromptBuilder
    ){
        this.semanticRetrievalService = semanticRetrievalService;
        this.ragPromptBuilder = ragPromptBuilder;
    }

    public RagContext build(
            Long userId,
            SemanticSearchRequest request
    ) {
        SourceSelectionMode mode = ragPromptBuilder.resolveMode(request.selectionMode());

        List<RetrievedSource> sources = semanticRetrievalService.retrieve(userId, request);

        if (sources.isEmpty()) {
            if (mode == SourceSelectionMode.EXCLUSIVE) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, INSUFFICIENT_SOURCES);
            }

            return new RagContext("", List.of());
        }

        StringBuilder context = getStringBuilder(sources);

        return new RagContext(context.toString(), sources);
    }

    private static @NonNull StringBuilder getStringBuilder(List<RetrievedSource> sources) {
        StringBuilder context = new StringBuilder();

        for (int i = 0; i < sources.size(); i++) {
            RetrievedSource source = sources.get(i);

            context.append("""
                            Fonte %d
                            Conteúdo: %s
                            Tópico: %s
                            Matéria: %s
                            """.formatted(
                                    i + 1,
                                    source.contentTitle(),
                                    source.topicName(),
                                    source.subjectName()
                            )
            );

            if (source.documentId() != null) {
                context.append("Documento: %s%n".formatted(source.documentName()));
            }

            context.append("""
                Trecho:
                %s
                
                """.formatted(source.text())
            );
        }
        return context;
    }
}