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
                2,
                ""
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
    void shouldIncludeAdditionalContextInFlashcardGenerationPrompt() {
        String theme = "Algoritmos";
        int quantity = 1;
        String generationContext = """
                Conteúdo de estudo selecionado:
                Título: Algoritmos e Máquinas Analíticas
                Conteúdo: Um algoritmo descreve uma sequência finita de instruções para resolver um problema.
                """.trim();

        String generatedContent = """
                PERGUNTA: O que é um algoritmo?
                RESPOSTA: É uma sequência finita de instruções para resolver um problema.
                ---
                """;

        String systemPrompt = """
                Você é um assistente de estudos multidisciplinar que gera flashcards.
                Regras:
                1. Gere exatamente a quantidade solicitada de flashcards.
                2. Todos os flashcards devem pertencer ao tema informado.
                3. O tema do baralho define o escopo conceitual principal da geração.
                4. Quando houver contexto adicional, utilize-o para orientar ou delimitar a geração\s
                sem substituir o tema do baralho.
                5. Cada flashcard deve conter uma pergunta clara e uma resposta conceitualmente correta.
                6. As perguntas devem abordar conhecimentos relevantes para estudo e revisão.
                7. As respostas devem ser curtas, objetivas e suficientes para responder à pergunta.
                8. Não repita perguntas.
                9. Responda em português brasileiro.
                10. Não inclua introduções, explicações adicionais ou blocos Markdown.
                11. Retorne cada flashcard exatamente no formato:

                PERGUNTA: <pergunta>
                RESPOSTA: <resposta>
                ---
               \s""";

        String userPrompt = """
                Tema do baralho:
                %s

                Quantidade de flashcards:
                %d

                Contexto adicional:
                %s
                """.formatted(
                theme,
                quantity,
                generationContext
        );

        when(generativeService.generate(systemPrompt, userPrompt)).thenReturn(generatedContent);

        List<GeneratedFlashcard> result = flashcardAiService.generateFlashcards(
                theme,
                quantity,
                generationContext
        );

        assertEquals(1, result.size());
        assertEquals("O que é um algoritmo?", result.get(0).question());
        assertEquals(
                "É uma sequência finita de instruções para resolver um problema.",
                result.get(0).answer()
        );

        verify(generativeService).generate(systemPrompt, userPrompt);
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
                () -> flashcardAiService.generateFlashcards("Sequências Numéricas", 1, "")
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
                () -> flashcardAiService.generateFlashcards("Sequências Numéricas", 2, "")
        );

        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatusCode());
    }
}