package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.dto.ContentGenerationRequest;
import br.com.studyassistant.studyassistantai.dto.GeneratedContentResponse;
import br.com.studyassistant.studyassistantai.model.Topic;
import br.com.studyassistant.studyassistantai.repository.TopicRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ContentAiService {

    private final GenerativeService generativeService;
    private final TopicRepository topicRepository;

    public ContentAiService(
            GenerativeService generativeService,
            TopicRepository topicRepository
    ) {
        this.generativeService = generativeService;
        this.topicRepository = topicRepository;
    }

    public GeneratedContentResponse generateContent(
            Long userId,
            ContentGenerationRequest request
    ) {
        Topic topic = topicRepository.findByIdAndUserId(request.topicId(), userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Topic not found"));

        String systemPrompt = """
                Você é um assistente de estudos multidisciplinar que gera materiais de estudo.
                Regras:
                1. Gere um conteúdo de estudo que atenda diretamente à solicitação do usuário.
                2. Considere o tópico informado como o assunto principal do conteúdo.
                3. Utilize o contexto fornecido pelo usuário somente quando ele estiver presente e for relevante.
                4. Não invente informações para preencher lacunas do contexto fornecido.
                5. Produza conteúdo claro, organizado e conceitualmente preciso.
                6. Não inclua comentários sobre o processo de geração.
                7. Responda em português brasileiro.
                8. Retorne exatamente no formato:
                TÍTULO: <título>
                CONTEÚDO:
                <conteúdo>
                """;

        String context = request.context() == null || request.context().isBlank()
                ? "Nenhum contexto adicional fornecido."
                : request.context();

        String userPrompt = """
                Tópico:
                %s

                Solicitação:
                %s

                Contexto fornecido pelo usuário:
                %s
                """.formatted(
                topic.getName(),
                request.request(),
                context
        );

        String generatedContent = generativeService.generate(systemPrompt, userPrompt);

        return parseGeneratedContent(request.topicId(), generatedContent);
    }

    private GeneratedContentResponse parseGeneratedContent(
            Long topicId,
            String generatedContent
    ) {
        String titlePrefix = "TÍTULO:";
        String contentPrefix = "CONTEÚDO:";

        int titleIndex = generatedContent.indexOf(titlePrefix);
        int contentIndex = generatedContent.indexOf(contentPrefix);

        if (titleIndex == -1 || contentIndex == -1 || contentIndex <= titleIndex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI returned an invalid content format");
        }

        String title = generatedContent
                .substring(titleIndex + titlePrefix.length(), contentIndex)
                .trim();

        String body = generatedContent
                .substring(contentIndex + contentPrefix.length())
                .trim();

        if (title.isBlank() || body.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI returned an invalid content format");
        }

        return new GeneratedContentResponse(topicId, title, body);
    }
}