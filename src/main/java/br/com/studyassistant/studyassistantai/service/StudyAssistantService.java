package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.dto.StudyAssistantRequest;
import br.com.studyassistant.studyassistantai.dto.StudyAssistantResponse;
import org.springframework.stereotype.Service;

@Service
public class StudyAssistantService {

    private final GenerativeService generativeService;

    public StudyAssistantService(GenerativeService generativeService) {
        this.generativeService = generativeService;
    }

    public StudyAssistantResponse ask(StudyAssistantRequest request) {
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

        String userPrompt = """
                Solicitação:
                %s

                Contexto fornecido pelo usuário:
                %s
                """.formatted(
                request.request(),
                context
        );

        String response = generativeService.generate(systemPrompt, userPrompt); // v1

        return new StudyAssistantResponse(response);
    }
}