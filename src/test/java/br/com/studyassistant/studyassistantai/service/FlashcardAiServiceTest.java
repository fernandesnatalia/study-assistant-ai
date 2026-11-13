package br.com.studyassistant.studyassistantai.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
}