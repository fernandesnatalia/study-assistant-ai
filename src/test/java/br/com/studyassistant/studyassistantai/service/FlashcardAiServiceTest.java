package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.dto.GeneratedFlashcard;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlashcardAiServiceTest {

    @Mock
    private GenerativeService generativeService;

    @InjectMocks
    private FlashcardAiService flashcardAiService;

    @Test
    void shouldGenerateAnswerUsingQuestionAndDeckTheme() {
        String question = "O que é um algoritmo?";
        String theme = "Algoritmos";
        String expectedAnswer = "É uma sequência finita de instruções para resolver um problema.";

        String systemPrompt = """
                Você é um assistente de estudos multidisciplinar que gera respostas de perguntas para flashcards.
                Regras:
                1. Responda diretamente à pergunta apresentada.
                2. Utilize o tema do baralho somente como contexto quando for relevante para a pergunta.
                3. Não confunda o tema do baralho com o assunto da pergunta.
                4. Produza respostas curtas e objetivas.
                5. Evite introduções, repetições, conclusões e explicações desnecessárias ou extensas demais.
                6. Se a pergunta não puder ser respondida com segurança, informe isso.
                7. Responda em português brasileiro, com precisão conceitual.
                """;

        String userPrompt = """
                Pergunta:
                %s

                Tema do baralho:
                %s
                """.formatted(question, theme);

        when(generativeService.generate(systemPrompt, userPrompt)).thenReturn(expectedAnswer);

        String result = flashcardAiService.generateAnswer(question, theme);

        assertEquals(expectedAnswer, result);

        verify(generativeService).generate(systemPrompt, userPrompt);
    }

    @Test
    void shouldGenerateRequestedNumberOfFlashcards() {
        String generatedContent = """
                PERGUNTA: O que é uma sequência numérica?
                RESPOSTA: É uma lista ordenada de números definida por uma regra.
                ---
                PERGUNTA: O que é uma progressão aritmética?
                RESPOSTA: É uma sequência em que a diferença entre termos consecutivos é constante.
                ---
                """;

        when(generativeService.generate(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString()
        )).thenReturn(generatedContent);

        List<GeneratedFlashcard> result = flashcardAiService.generateFlashcards(
                "Sequências Numéricas",
                2
        );

        assertEquals(2, result.size());
        assertEquals("O que é uma sequência numérica?", result.get(0).question());
        assertEquals(
                "É uma lista ordenada de números definida por uma regra.",
                result.get(0).answer()
        );
        assertEquals("O que é uma progressão aritmética?", result.get(1).question());
        assertEquals(
                "É uma sequência em que a diferença entre termos consecutivos é constante.",
                result.get(1).answer()
        );
    }

    @Test
    void shouldRejectInvalidGeneratedFlashcardFormat() {
        String generatedContent = """
                O que é uma sequência numérica?
                É uma lista ordenada de números.
                """;

        when(generativeService.generate(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString()
        )).thenReturn(generatedContent);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> flashcardAiService.generateFlashcards("Sequências Numéricas", 1)
        );

        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatusCode());
    }

    @Test
    void shouldRejectGeneratedQuantityDifferentFromRequestedQuantity() {
        String generatedContent = """
                PERGUNTA: O que é uma sequência numérica?
                RESPOSTA: É uma lista ordenada de números definida por uma regra.
                ---
                """;

        when(generativeService.generate(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString()
        )).thenReturn(generatedContent);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> flashcardAiService.generateFlashcards("Sequências Numéricas", 2)
        );

        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatusCode());
    }
}