package br.com.studyassistant.studyassistantai.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.ai.chat.client.ChatClient;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenerativeServiceTest {

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private ChatClient chatClient;

    private ChatClient.ChatClientRequestSpec requestSpec;

    private ChatClient.CallResponseSpec responseSpec;

    private GenerativeService generativeService;

    @BeforeEach
    void setUp() {
        requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        responseSpec = mock(ChatClient.CallResponseSpec.class);

        when(chatClientBuilder.build()).thenReturn(chatClient);

        generativeService = new GenerativeService(chatClientBuilder);
    }

    @Test
    void shouldGenerateAnswerUsingQuestionAndReferenceContent() {
        String question = "O que é uma equação do segundo grau?";
        String content = "Uma equação do segundo grau possui a forma ax² + bx + c = 0.";
        String expectedAnswer = "É uma equação polinomial de grau dois.";

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn(expectedAnswer);

        String result = generativeService.generateAnswer(question, content);

        assertEquals(expectedAnswer, result);

        verify(requestSpec).system(anyString());
        verify(requestSpec).user(
                """
                Pergunta:
                %s
        
                Conteúdo de referência:
                %s
                """.formatted(question, content)
        );
        verify(requestSpec).call();
        verify(responseSpec).content();
    }
}