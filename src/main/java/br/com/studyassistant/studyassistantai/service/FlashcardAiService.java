package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.prompt.AiPromptBuilder;
import br.com.studyassistant.studyassistantai.dto.GeneratedFlashcard;
import br.com.studyassistant.studyassistantai.dto.RagContext;
import br.com.studyassistant.studyassistantai.enums.PromptPurpose;
import br.com.studyassistant.studyassistantai.enums.SourceSelectionMode;
import br.com.studyassistant.studyassistantai.prompt.RagPromptBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static br.com.studyassistant.studyassistantai.constants.ErrorMessages.*;

@Service
public class FlashcardAiService {

    private final GenerativeService generativeService;
    private final RagPromptBuilder ragPromptBuilder;
    private final AiPromptBuilder aiPromptBuilder;

    public FlashcardAiService(
            GenerativeService generativeService,
            RagPromptBuilder ragPromptBuilder,
            AiPromptBuilder aiPromptBuilder
    ) {
        this.generativeService = generativeService;
        this.ragPromptBuilder = ragPromptBuilder;
        this.aiPromptBuilder = aiPromptBuilder;
    }

    public String generateAnswer(String question, String theme) {
        String systemPrompt = aiPromptBuilder.flashcardAnswerSystemPrompt();
        String userPrompt = aiPromptBuilder.flashcardAnswerUserPrompt(question, theme);

        return generativeService.generate(systemPrompt, userPrompt);
    }

    public List<GeneratedFlashcard> generateFlashcards(
            String theme,
            int quantity,
            String generationContext
    ) {
        return generateFlashcards(theme, quantity, generationContext, null, null);
    }

    public List<GeneratedFlashcard> generateFlashcards(
            String theme,
            int quantity,
            String generationContext,
            RagContext ragContext,
            SourceSelectionMode selectionMode
    ) {
        String systemPrompt = aiPromptBuilder.flashcardGenerationSystemPrompt();
        String userPrompt = aiPromptBuilder.flashcardGenerationUserPrompt(theme, quantity, generationContext);

        if (ragContext != null) {
            userPrompt += ragPromptBuilder.build(ragContext, selectionMode, PromptPurpose.FLASHCARD);
        }

        String generatedContent = generativeService.generate(systemPrompt, userPrompt);

        if (ragContext != null
                && ragPromptBuilder.resolveMode(selectionMode) == SourceSelectionMode.EXCLUSIVE
                && generatedContent != null
                && generatedContent.trim().equals("FONTES_INSUFICIENTES")) {

            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, INSUFFICIENT_SOURCES);}

        return parseGeneratedFlashcards(generatedContent, quantity);
    }

    private List<GeneratedFlashcard> parseGeneratedFlashcards(
            String generatedContent,
            int expectedQuantity
    ) {
        if (generatedContent == null || generatedContent.isBlank()) {
            throw invalidAiResponse();
        }

        String normalizedContent = generatedContent
                .replace("\r\n", "\n")
                .replace('\r', '\n');

        Pattern pattern = Pattern.compile(
                "(?m)^\\s*PERGUNTA:\\s*(.*?)\\s*\\n" +
                        "\\s*RESPOSTA:\\s*([\\s\\S]*?)" +
                        "(?=^\\s*(?:---|PERGUNTA:)|\\z)"
        );

        Matcher matcher = pattern.matcher(normalizedContent);
        List<GeneratedFlashcard> flashcards = new ArrayList<>();

        while (matcher.find()) {
            String question = matcher.group(1).trim();
            String answer = matcher.group(2).trim();

            if (question.isBlank() || answer.isBlank()) {
                throw invalidAiResponse();
            }

            flashcards.add(new GeneratedFlashcard(question, answer));
        }

        if (flashcards.size() != expectedQuantity) {
            throw invalidAiResponse();
        }

        return flashcards;
    }

    private ResponseStatusException invalidAiResponse() {
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY, INVALID_FLASHCARD_FORMAT);
    }
}