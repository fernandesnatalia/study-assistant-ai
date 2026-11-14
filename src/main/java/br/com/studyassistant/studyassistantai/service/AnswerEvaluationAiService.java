package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.model.AnswerAttemptResult;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AnswerEvaluationAiService {

    private final GenerativeService generativeService;

    public AnswerEvaluationAiService(GenerativeService generativeService) {
        this.generativeService = generativeService;
    }

    public AnswerAttemptResult evaluate(
            String question,
            String expectedAnswer,
            String userAnswer
    ) {
        String systemPrompt = """
                Você é um avaliador de respostas de estudo.
                Compare a resposta do usuário com a resposta esperada considerando o significado conceitual,\s
                e não apenas correspondência literal.

                Regras:
                1. Retorne CORRECT quando a resposta do usuário estiver conceitualmente correta e suficiente.
                2. Retorne INCORRECT quando estiver incorreta, contraditória ou não responder adequadamente à pergunta.
                3. Não exija que a redação seja idêntica à resposta esperada.
                4. Não forneça explicações, comentários ou justificativas.
                5. Retorne exatamente uma destas opções:
                CORRECT
                INCORRECT
               \s""";

        String userPrompt = """
                Pergunta:
                %s

                Resposta esperada:
                %s

                Resposta do usuário:
                %s
                """.formatted(
                question,
                expectedAnswer,
                userAnswer
        );

        String result = generativeService.generate(systemPrompt, userPrompt).trim();

        try {
            return AnswerAttemptResult.valueOf(result);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI returned an invalid answer evaluation");
        }
    }
}