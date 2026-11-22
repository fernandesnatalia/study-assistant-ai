package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.dto.ContentGenerationRequest;
import br.com.studyassistant.studyassistantai.dto.GeneratedContentResponse;
import br.com.studyassistant.studyassistantai.dto.RagContext;
import br.com.studyassistant.studyassistantai.enums.PromptPurpose;
import br.com.studyassistant.studyassistantai.model.Topic;
import br.com.studyassistant.studyassistantai.prompt.AiPromptBuilder;
import br.com.studyassistant.studyassistantai.prompt.RagPromptBuilder;
import br.com.studyassistant.studyassistantai.repository.TopicRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import static br.com.studyassistant.studyassistantai.constants.ErrorMessages.INVALID_CONTENT_FORMAT;
import static br.com.studyassistant.studyassistantai.constants.ErrorMessages.TOPIC_NOT_FOUND;

@Service
public class ContentAiService {

    private final RagPromptBuilder ragPromptBuilder;
    private final AiPromptBuilder aiPromptBuilder;
    private final RagContextService ragContextService;
    private final GenerativeService generativeService;
    private final TopicRepository topicRepository;

    public ContentAiService(
            RagPromptBuilder ragPromptBuilder,
            AiPromptBuilder aiPromptBuilder,
            RagContextService ragContextService,
            GenerativeService generativeService,
            TopicRepository topicRepository
    ) {
        this.ragPromptBuilder = ragPromptBuilder;
        this.aiPromptBuilder = aiPromptBuilder;
        this.ragContextService = ragContextService;
        this.generativeService = generativeService;
        this.topicRepository = topicRepository;
    }

    public GeneratedContentResponse generateContent(
            Long userId,
            ContentGenerationRequest request
    ) {
        Topic topic = topicRepository.findByIdAndUserId(request.topicId(), userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, TOPIC_NOT_FOUND));

        RagContext ragContext = request.semanticSearch() == null ?
                new RagContext("", List.of()) : ragContextService.build(userId, request.semanticSearch()
        );

        String systemPrompt = aiPromptBuilder.contentSystemPrompt();
        String userPrompt = aiPromptBuilder.contentUserPrompt(topic.getName(), request.request(), request.context());

        if (request.semanticSearch() != null) {
            userPrompt += ragPromptBuilder.build(
                    ragContext, request.semanticSearch().selectionMode(), PromptPurpose.CONTENT
            );
        }

        String generatedContent = generativeService.generate(systemPrompt, userPrompt);

        GeneratedContentResponse generated = parseGeneratedContent(request.topicId(), generatedContent);

        return new GeneratedContentResponse(
                generated.topicId(), generated.title(), generated.body(), ragContext.sources()
        );
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
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, INVALID_CONTENT_FORMAT);
        }

        String title = generatedContent
                .substring(titleIndex + titlePrefix.length(), contentIndex)
                .trim();

        String body = generatedContent
                .substring(contentIndex + contentPrefix.length())
                .trim();

        if (title.isBlank() || body.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, INVALID_CONTENT_FORMAT);
        }

        return new GeneratedContentResponse(topicId, title, body, List.of());
    }
}