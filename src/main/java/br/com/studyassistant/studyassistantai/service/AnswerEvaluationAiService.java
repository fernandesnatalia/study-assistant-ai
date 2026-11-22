package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.prompt.AiPromptBuilder;
import br.com.studyassistant.studyassistantai.enums.AnswerAttemptResult;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import static br.com.studyassistant.studyassistantai.constants.ErrorMessages.INVALID_EVALUATION;

@Service
public class AnswerEvaluationAiService {

    private final GenerativeService generativeService;
    private final AiPromptBuilder aiPromptBuilder;

    public AnswerEvaluationAiService(
            GenerativeService generativeService,
            AiPromptBuilder aiPromptBuilder
    ) {
        this.generativeService = generativeService;
        this.aiPromptBuilder = aiPromptBuilder;
    }

    public AnswerAttemptResult evaluate(
            String question,
            String expectedAnswer,
            String userAnswer
    ) {
        String systemPrompt = aiPromptBuilder.answerEvaluationSystemPrompt();
        String userPrompt = aiPromptBuilder.answerEvaluationUserPrompt(question, expectedAnswer, userAnswer);
        String result = generativeService.generate(systemPrompt, userPrompt).trim();

        try {
            return AnswerAttemptResult.valueOf(result);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, INVALID_EVALUATION);
        }
    }
}