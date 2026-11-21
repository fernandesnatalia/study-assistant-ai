package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.dto.RagContext;
import br.com.studyassistant.studyassistantai.dto.SemanticSearchRequest;
import br.com.studyassistant.studyassistantai.dto.StudyAssistantRequest;
import br.com.studyassistant.studyassistantai.dto.StudyAssistantResponse;
import br.com.studyassistant.studyassistantai.enums.SourceSelectionMode;
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
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudyAssistantServiceTest {

    @Mock
    private GenerativeService generativeService;

    @Mock
    private RagContextService ragContextService;

    @InjectMocks
    private StudyAssistantService studyAssistantService;

    @Spy
    private RagPromptBuilder ragPromptBuilder = new RagPromptBuilder();

    private void mockEmptySources() {
        when(ragContextService.build(any(), any())).thenReturn(new RagContext("", List.of()));
    }

    @Test
    void shouldAnswerStudyRequestWithContext() {
        StudyAssistantRequest request = new StudyAssistantRequest(
                "Explique por que todo corpo é um anel.",
                "Estou estudando estruturas algébricas.",
                null, null, null, null, null, null
        );

        mockEmptySources();

        when(generativeService.generate(anyString(), anyString()))
                .thenReturn("Todo corpo satisfaz os axiomas de anel.");

        StudyAssistantResponse result = studyAssistantService.ask(1L, request);

        assertEquals("Todo corpo satisfaz os axiomas de anel.", result.response());
        assertTrue(result.sources().isEmpty());

        verify(ragContextService).build(eq(1L), any(SemanticSearchRequest.class));
    }

    @Test
    void shouldAnswerStudyRequestWithoutContext() {
        StudyAssistantRequest request = new StudyAssistantRequest(
                "Explique o que é um anel comutativo.",
                null, null, null, null, null, null, null
        );

        mockEmptySources();

        when(generativeService.generate(anyString(), anyString()))
                .thenReturn("Um anel cuja multiplicação é comutativa.");

        StudyAssistantResponse result = studyAssistantService.ask(1L, request);

        assertEquals("Um anel cuja multiplicação é comutativa.", result.response());
        assertTrue(result.sources().isEmpty());
    }

    @Test
    void shouldUseProvidedContextInPrompt() {
        StudyAssistantRequest request = new StudyAssistantRequest(
                "Explique esse conceito de outra forma.",
                "Um anel comutativo possui multiplicação comutativa.",
                null, null, null, null, null, null
        );

        mockEmptySources();

        when(generativeService.generate(anyString(), anyString())).thenReturn("Explicação reformulada.");

        studyAssistantService.ask(1L, request);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        verify(generativeService).generate(anyString(), captor.capture());

        assertTrue(captor.getValue().contains("Um anel comutativo possui multiplicação comutativa."));
    }

    @Test
    void shouldUseDefaultMessageWhenContextIsBlank() {
        StudyAssistantRequest request = new StudyAssistantRequest(
                "Explique o conceito de anel.",
                " ", null, null, null, null, null, null
        );

        mockEmptySources();

        when(generativeService.generate(anyString(), anyString()))
                .thenReturn("Um anel é uma estrutura algébrica.");

        studyAssistantService.ask(1L, request);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        verify(generativeService).generate(anyString(), captor.capture());

        assertTrue(captor.getValue().contains("Nenhum contexto adicional fornecido."));
    }

    @Test
    void shouldAutomaticallyRetrieveSourcesWithoutSelection() {
        StudyAssistantRequest request = new StudyAssistantRequest(
                "Explique algoritmos.",
                null, null, null, null, null, null, null
        );

        mockEmptySources();

        when(generativeService.generate(anyString(), anyString())).thenReturn("Explicação.");

        studyAssistantService.ask(1L, request);

        ArgumentCaptor<SemanticSearchRequest> captor = ArgumentCaptor.forClass(SemanticSearchRequest.class);

        verify(ragContextService).build(eq(1L), captor.capture());

        SemanticSearchRequest search = captor.getValue();

        assertEquals("Explique algoritmos.", search.query());
        assertEquals(SourceSelectionMode.PREFERRED, search.selectionMode());
        assertNull(search.subjectsIds());
        assertNull(search.topicsIds());
        assertNull(search.contentsIds());
        assertNull(search.topK());
        assertNull(search.similarityThreshold());
    }

    @Test
    void shouldApplySelectedSourceFilters() {
        StudyAssistantRequest request = new StudyAssistantRequest(
                "Explique algoritmos.",
                null, Set.of(1L), Set.of(2L), Set.of(3L), SourceSelectionMode.PREFERRED, null, null
        );

        mockEmptySources();

        when(generativeService.generate(anyString(), anyString())).thenReturn("Explicação.");

        studyAssistantService.ask(1L, request);

        ArgumentCaptor<SemanticSearchRequest> captor = ArgumentCaptor.forClass(SemanticSearchRequest.class);

        verify(ragContextService).build(eq(1L), captor.capture());

        SemanticSearchRequest search = captor.getValue();

        assertEquals(Set.of(1L), search.subjectsIds());
        assertEquals(Set.of(2L), search.topicsIds());
        assertEquals(Set.of(3L), search.contentsIds());
    }

    @Test
    void shouldUseRetrievedSourcesInPreferredMode() {
        StudyAssistantRequest request = new StudyAssistantRequest(
                "Explique algoritmos.", null, null, null, null, SourceSelectionMode.PREFERRED, null, null
        );

        RagContext ragContext = new RagContext("Fonte 1\nAlgoritmos são sequências de instruções.", List.of());

        when(ragContextService.build(eq(1L), any())).thenReturn(ragContext);
        when(generativeService.generate(anyString(), anyString())).thenReturn("Algoritmos são sequências de instruções.");

        StudyAssistantResponse result = studyAssistantService.ask(1L, request);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        verify(generativeService).generate(anyString(), captor.capture());

        assertTrue(captor.getValue().contains("Algoritmos são sequências de instruções."));
        assertTrue(captor.getValue().contains("Priorize as fontes recuperadas"));
        assertEquals(ragContext.sources(), result.sources());
    }

    @Test
    void shouldUseExclusiveSourceInstructions() {
        StudyAssistantRequest request = new StudyAssistantRequest(
                "Explique algoritmos.",
                null, null, null, null, SourceSelectionMode.EXCLUSIVE, null, null
        );

        when(ragContextService.build(eq(1L), any()))
                .thenReturn(new RagContext("Fonte 1\nTexto recuperado.", List.of()));
        when(generativeService.generate(anyString(), anyString()))
                .thenReturn("Resposta fundamentada.");

        studyAssistantService.ask(1L, request);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        verify(generativeService).generate(anyString(), captor.capture());

        assertTrue(captor.getValue().contains("Utilize exclusivamente as fontes recuperadas"));
    }

    @Test
    void shouldRejectExclusiveRequestWithoutSources() {
        StudyAssistantRequest request = new StudyAssistantRequest(
                "Explique algoritmos.", null, null, null, null, SourceSelectionMode.EXCLUSIVE, null, null
        );

        when(ragContextService.build(eq(1L), any()))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.UNPROCESSABLE_CONTENT, "Insufficient sources for exclusive generation")
                );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> studyAssistantService.ask(1L, request)
        );

        assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, exception.getStatusCode());
        verifyNoInteractions(generativeService);
    }

    @Test
    void shouldContinueWithoutSourcesInPreferredMode() {
        StudyAssistantRequest request = new StudyAssistantRequest(
                "Explique algoritmos.", null, null, null, null, SourceSelectionMode.PREFERRED, null, null
        );

        mockEmptySources();

        when(generativeService.generate(anyString(), anyString()))
                .thenReturn("Explicação com conhecimento geral.");

        StudyAssistantResponse result = studyAssistantService.ask(1L, request);

        assertEquals("Explicação com conhecimento geral.", result.response());
        assertTrue(result.sources().isEmpty());

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        verify(generativeService).generate(anyString(), captor.capture());

        assertTrue(captor.getValue().contains("Nenhuma fonte relevante encontrada."));
    }
}