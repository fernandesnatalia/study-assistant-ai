package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.dto.StudyAssistantRequest;
import br.com.studyassistant.studyassistantai.dto.StudyAssistantResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudyAssistantServiceTest {

    @Mock
    private GenerativeService generativeService;

    @InjectMocks
    private StudyAssistantService studyAssistantService;

    @Test
    void shouldAnswerStudyRequestWithContext() {
        StudyAssistantRequest request = new StudyAssistantRequest(
                "Explique por que todo corpo é um anel.",
                "Estou estudando estruturas algébricas e já conheço a definição de anel."
        );

        String generatedResponse = "Todo corpo é um anel porque satisfaz os axiomas exigidos para essa estrutura.";

        when(generativeService.generate(anyString(), anyString())).thenReturn(generatedResponse);

        StudyAssistantResponse result = studyAssistantService.ask(request);

        assertEquals(generatedResponse, result.response());

        verify(generativeService).generate(anyString(), anyString());
    }

    @Test
    void shouldAnswerStudyRequestWithoutContext() {
        StudyAssistantRequest request = new StudyAssistantRequest("Explique o que é um anel comutativo.", null);

        String generatedResponse = "Um anel comutativo é um anel cuja multiplicação é comutativa.";

        when(generativeService.generate(anyString(), anyString())).thenReturn(generatedResponse);

        StudyAssistantResponse result = studyAssistantService.ask(request);

        assertEquals(generatedResponse, result.response());

        verify(generativeService).generate(anyString(), anyString());
    }

    @Test
    void shouldUseProvidedContextInPrompt() {
        StudyAssistantRequest request = new StudyAssistantRequest(
                "Explique esse conceito de outra forma.",
                "Um anel comutativo possui multiplicação comutativa."
        );

        when(generativeService.generate(anyString(), anyString())).thenReturn("Explicação reformulada.");

        studyAssistantService.ask(request);

        ArgumentCaptor<String> userPromptCaptor = ArgumentCaptor.forClass(String.class);

        verify(generativeService).generate(anyString(), userPromptCaptor.capture());

        String userPrompt = userPromptCaptor.getValue();

        assertTrue(userPrompt.contains("Explique esse conceito de outra forma."));
        assertTrue(userPrompt.contains("Um anel comutativo possui multiplicação comutativa."));
    }

    @Test
    void shouldUseDefaultMessageWhenContextIsBlank() {
        StudyAssistantRequest request = new StudyAssistantRequest("Explique o conceito de anel.", " ");

        when(generativeService.generate(anyString(), anyString())).thenReturn("Um anel é uma estrutura algébrica.");

        studyAssistantService.ask(request);

        ArgumentCaptor<String> userPromptCaptor = ArgumentCaptor.forClass(String.class);

        verify(generativeService).generate(anyString(), userPromptCaptor.capture());

        assertTrue(userPromptCaptor.getValue().contains("Nenhum contexto adicional fornecido."));
    }
}