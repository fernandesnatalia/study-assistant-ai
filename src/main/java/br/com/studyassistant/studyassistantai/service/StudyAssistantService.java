package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.dto.RagContext;
import br.com.studyassistant.studyassistantai.dto.SemanticSearchRequest;
import br.com.studyassistant.studyassistantai.dto.StudyAssistantRequest;
import br.com.studyassistant.studyassistantai.dto.StudyAssistantResponse;
import br.com.studyassistant.studyassistantai.enums.PromptPurpose;
import br.com.studyassistant.studyassistantai.model.SourceSelectionMode;
import br.com.studyassistant.studyassistantai.prompt.RagPromptBuilder;
import org.springframework.stereotype.Service;

@Service
public class StudyAssistantService {

    private final GenerativeService generativeService;
    private final RagContextService ragContextService;
    private final RagPromptBuilder ragPromptBuilder;

    public StudyAssistantService(
            GenerativeService generativeService,
            RagContextService ragContextService,
            RagPromptBuilder ragPromptBuilder
    ) {
        this.generativeService = generativeService;
        this.ragContextService = ragContextService;
        this.ragPromptBuilder = ragPromptBuilder;
    }

    public StudyAssistantResponse ask(Long userId, StudyAssistantRequest request) {
        String systemPrompt = """
                Você é um assistente de estudos multidisciplinar.
                Regras:
                1. Responda diretamente à solicitação do usuário.
                2. Utilize o contexto fornecido pelo usuário quando estiver presente e for relevante.
                3. Não invente informações para preencher lacunas do contexto fornecido.
                4. Forneça explicações claras, conceitualmente precisas e adequadas ao estudo.
                5. Quando solicitado, aprofunde conceitos ou apresente a informação de outra forma.
                6. Não inclua comentários sobre o processo de geração.
                7. Responda em português brasileiro.
                """;

        String context = request.context() == null || request.context().isBlank()
                ? "Nenhum contexto adicional fornecido."
                : request.context();

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

        String userPrompt = """
        Solicitação:
        %s

        Contexto fornecido pelo usuário:
        %s
        """.formatted(request.request(), context);

        userPrompt += ragPromptBuilder.build(ragContext, mode, PromptPurpose.STUDY_ASSISTANT);

        String response = generativeService.generate(systemPrompt, userPrompt);

        return new StudyAssistantResponse(response, ragContext.sources());
    }
}