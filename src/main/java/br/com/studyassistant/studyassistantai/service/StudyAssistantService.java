package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.dto.RagContext;
import br.com.studyassistant.studyassistantai.dto.SemanticSearchRequest;
import br.com.studyassistant.studyassistantai.dto.StudyAssistantRequest;
import br.com.studyassistant.studyassistantai.dto.StudyAssistantResponse;
import br.com.studyassistant.studyassistantai.enums.PromptPurpose;
import br.com.studyassistant.studyassistantai.enums.SourceSelectionMode;
import br.com.studyassistant.studyassistantai.prompt.AiPromptBuilder;
import br.com.studyassistant.studyassistantai.prompt.RagPromptBuilder;
import org.springframework.stereotype.Service;

@Service
public class StudyAssistantService {

    private final GenerativeService generativeService;
    private final RagContextService ragContextService;
    private final RagPromptBuilder ragPromptBuilder;
    private final AiPromptBuilder aiPromptBuilder;

    public StudyAssistantService(
            GenerativeService generativeService,
            RagContextService ragContextService,
            RagPromptBuilder ragPromptBuilder,
            AiPromptBuilder aiPromptBuilder
    ) {
        this.generativeService = generativeService;
        this.ragContextService = ragContextService;
        this.ragPromptBuilder = ragPromptBuilder;
        this.aiPromptBuilder = aiPromptBuilder;
    }

    public StudyAssistantResponse ask(Long userId, StudyAssistantRequest request) {
        String systemPrompt = aiPromptBuilder.studyAssistantSystemPrompt();

        SourceSelectionMode mode = ragPromptBuilder.resolveMode(request.selectionMode());

        SemanticSearchRequest semanticSearch = new SemanticSearchRequest(
                request.request(),
                request.subjectsIds(),
                request.topicsIds(),
                request.contentsIds(),
                mode,
                request.topK(),
                request.similarityThreshold()
        );

        RagContext ragContext = ragContextService.build(userId, semanticSearch);

        String userPrompt = aiPromptBuilder.studyAssistantUserPrompt(request.request(), request.context());

        userPrompt += ragPromptBuilder.build(ragContext, mode, PromptPurpose.STUDY_ASSISTANT);

        String response = generativeService.generate(systemPrompt, userPrompt);

        return new StudyAssistantResponse(response, ragContext.sources());
    }
}