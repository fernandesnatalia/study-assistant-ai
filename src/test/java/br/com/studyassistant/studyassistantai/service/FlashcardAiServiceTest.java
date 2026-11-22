package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.dto.GeneratedFlashcard;
import br.com.studyassistant.studyassistantai.dto.RagContext;
import br.com.studyassistant.studyassistantai.enums.SourceSelectionMode;
import br.com.studyassistant.studyassistantai.prompt.AiPromptBuilder;
import br.com.studyassistant.studyassistantai.prompt.RagPromptBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlashcardAiServiceTest {

    @Mock
    private GenerativeService generativeService;

    @InjectMocks
    private FlashcardAiService flashcardAiService;

    @Spy
    private RagPromptBuilder ragPromptBuilder = new RagPromptBuilder();

    @Spy
    private AiPromptBuilder aiPromptBuilder = new AiPromptBuilder();

    private static final String GENERATIVE_CONTENT = """
                    PERGUNTA: O que é um algoritmo?
                    RESPOSTA: É uma sequência finita de instruções.
                    ---
                    """;

    @Test
    void shouldGenerateAnswerUsingQuestionAndDeckTheme() {
        String question = "O que é um algoritmo?";
        String theme = "Algoritmos";
        String expectedAnswer = "É uma sequência finita de instruções para resolver um problema.";

        String systemPrompt = aiPromptBuilder.flashcardAnswerSystemPrompt();
        String userPrompt = aiPromptBuilder.flashcardAnswerUserPrompt(question, theme);

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

        when(generativeService.generate(anyString(), anyString())).thenReturn(generatedContent);

        List<GeneratedFlashcard> result = flashcardAiService.generateFlashcards("Sequências Numéricas", 2, "");

        assertEquals(2, result.size());
        assertEquals("O que é uma sequência numérica?", result.get(0).question());
        assertEquals("É uma lista ordenada de números definida por uma regra.", result.get(0).answer());
        assertEquals("O que é uma progressão aritmética?", result.get(1).question());
        assertEquals("É uma sequência em que a diferença entre termos consecutivos é constante.", result.get(1).answer());
    }

    @Test
    void shouldIncludeAdditionalContextInFlashcardGenerationPrompt() {
        String theme = "Algoritmos";
        int quantity = 1;

        String systemPrompt = aiPromptBuilder.flashcardGenerationSystemPrompt();
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

        String userPrompt = aiPromptBuilder.flashcardGenerationUserPrompt(theme, quantity, generationContext);

        when(generativeService.generate(systemPrompt, userPrompt)).thenReturn(generatedContent);

        List<GeneratedFlashcard> result = flashcardAiService.generateFlashcards(theme, quantity, generationContext);

        assertEquals(1, result.size());
        assertEquals("O que é um algoritmo?", result.get(0).question());
        assertEquals("É uma sequência finita de instruções para resolver um problema.", result.get(0).answer());

        verify(generativeService).generate(systemPrompt, userPrompt);
    }

    @Test
    void shouldGenerateFlashcardsWithRetrievedSources() {
        RagContext ragContext = new RagContext("Fonte 1\nTrecho sobre algoritmos.", List.of());

        when(generativeService.generate(anyString(), anyString()))
                .thenReturn(GENERATIVE_CONTENT);

        List<GeneratedFlashcard> result = flashcardAiService
                .generateFlashcards("Algoritmos", 1, "", ragContext, SourceSelectionMode.PREFERRED);

        assertEquals(1, result.size());
        assertEquals("O que é um algoritmo?", result.get(0).question());

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);

        verify(generativeService).generate(anyString(), promptCaptor.capture());

        assertTrue(promptCaptor.getValue().contains("Trecho sobre algoritmos."));
        assertTrue(promptCaptor.getValue().contains("Priorize as fontes recuperadas"));
    }

    @Test
    void shouldUseExclusiveSourcesForFlashcardGeneration() {
        RagContext ragContext = new RagContext("Fonte 1\nTrecho sobre algoritmos.", List.of());

        when(generativeService.generate(anyString(), anyString())).thenReturn(GENERATIVE_CONTENT);

        flashcardAiService.generateFlashcards("Algoritmos", 1, "", ragContext, SourceSelectionMode.EXCLUSIVE);

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);

        verify(generativeService).generate(anyString(), promptCaptor.capture());

        assertTrue(promptCaptor.getValue().contains("Utilize exclusivamente as fontes recuperadas"));
    }

    @Test
    void shouldGenerateFlashcardsWithoutRagInstructions() {
        when(generativeService.generate(anyString(), anyString()))
                .thenReturn(GENERATIVE_CONTENT);

        List<GeneratedFlashcard> result = flashcardAiService.generateFlashcards("Algoritmos", 1, "");

        assertEquals(1, result.size());

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);

        verify(generativeService).generate(anyString(), promptCaptor.capture());

        assertFalse(promptCaptor.getValue().contains("Fontes recuperadas:"));
        assertFalse(promptCaptor.getValue().contains("Instruções de fundamentação:"));
    }

    @Test
    void shouldRejectInvalidGeneratedFlashcardFormat() {
        String generatedContent = """
                O que é uma sequência numérica?
                É uma lista ordenada de números.
                """;

        when(generativeService.generate(anyString(), anyString())).thenReturn(generatedContent);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () ->
                flashcardAiService.generateFlashcards("Sequências Numéricas", 1, "")
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

        when(generativeService.generate(anyString(), anyString())).thenReturn(generatedContent);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () ->
                flashcardAiService.generateFlashcards("Sequências Numéricas", 2, "")
        );

        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatusCode());
    }
}