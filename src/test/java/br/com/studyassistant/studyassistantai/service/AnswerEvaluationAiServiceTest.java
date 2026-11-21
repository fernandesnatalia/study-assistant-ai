package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.enums.AnswerAttemptResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnswerEvaluationAiServiceTest {

    @Mock
    private GenerativeService generativeService;

    @InjectMocks
    private AnswerEvaluationAiService answerEvaluationAiService;

    @Test
    void shouldReturnCorrectWhenAiEvaluatesAnswerAsCorrect() {
        when(generativeService.generate(anyString(), anyString())).thenReturn("CORRECT");

        AnswerAttemptResult result = answerEvaluationAiService.evaluate(
                "O que é um algoritmo?",
                "É uma sequência finita de instruções para resolver um problema.",
                "É uma sequência de passos para resolver um problema."
        );

        assertEquals(AnswerAttemptResult.CORRECT, result);
    }

    @Test
    void shouldReturnIncorrectWhenAiEvaluatesAnswerAsIncorrect() {
        when(generativeService.generate(anyString(), anyString())).thenReturn("INCORRECT");

        AnswerAttemptResult result = answerEvaluationAiService.evaluate(
                "O que é um algoritmo?",
                "É uma sequência finita de instruções para resolver um problema.",
                "É um tipo de banco de dados."
        );

        assertEquals(AnswerAttemptResult.INCORRECT, result);
    }

    @Test
    void shouldRejectInvalidAiEvaluation() {
        when(generativeService.generate(anyString(), anyString())).thenReturn("A resposta está correta.");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> answerEvaluationAiService.evaluate(
                        "O que é um algoritmo?",
                        "É uma sequência finita de instruções para resolver um problema.",
                        "É uma sequência de passos para resolver um problema."
                )
        );

        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatusCode());
    }
}