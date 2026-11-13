package br.com.studyassistant.studyassistantai.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void shouldGenerateContentUsingSystemAndUserPrompts() {
        String systemPrompt = "Você é um assistente de estudos.";
        String userPrompt = "Explique o que é um algoritmo.";
        String expectedResponse = "Um algoritmo é uma sequência finita de instruções.";

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(systemPrompt)).thenReturn(requestSpec);
        when(requestSpec.user(userPrompt)).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn(expectedResponse);

        String result = generativeService.generate(systemPrompt, userPrompt);

        assertEquals(expectedResponse, result);

        verify(requestSpec).system(systemPrompt);
        verify(requestSpec).user(userPrompt);
        verify(requestSpec).call();
        verify(responseSpec).content();
    }
}