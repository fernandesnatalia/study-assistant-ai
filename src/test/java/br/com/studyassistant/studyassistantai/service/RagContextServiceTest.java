package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.dto.RagContext;
import br.com.studyassistant.studyassistantai.dto.RetrievedSource;
import br.com.studyassistant.studyassistantai.dto.SemanticSearchRequest;
import br.com.studyassistant.studyassistantai.model.SourceSelectionMode;
import br.com.studyassistant.studyassistantai.prompt.RagPromptBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RagContextServiceTest {

    private SemanticRetrievalService semanticRetrievalService;
    private RagContextService ragService;
    private RagPromptBuilder ragPromptBuilder;

    @BeforeEach
    void setUp() {
        semanticRetrievalService = mock(SemanticRetrievalService.class);
        ragPromptBuilder = new RagPromptBuilder();
        ragService = new RagContextService(semanticRetrievalService, ragPromptBuilder);
    }

    @Test
    void shouldBuildContextWithRetrievedSources() {
        SemanticSearchRequest request = request(SourceSelectionMode.PREFERRED);

        RetrievedSource source = source(10L, null, null);

        when(semanticRetrievalService.retrieve(1L, request)).thenReturn(List.of(source));

        RagContext result = ragService.build(1L, request);

        assertEquals(List.of(source), result.sources());

        assertTrue(result.text().contains("Fonte 1"));
        assertTrue(result.text().contains("Conteúdo: Anéis"));
        assertTrue(result.text().contains("Tópico: Álgebra"));
        assertTrue(result.text().contains("Matéria: Matemática"));
        assertTrue(result.text().contains("Trecho sobre anéis"));

        verify(semanticRetrievalService).retrieve(1L, request);
    }

    @Test
    void shouldIncludeDocumentMetadata() {
        SemanticSearchRequest request = request(SourceSelectionMode.PREFERRED);

        RetrievedSource source = source(10L, 50L, "algebra.pdf");

        when(semanticRetrievalService.retrieve(1L, request)).thenReturn(List.of(source));

        RagContext result = ragService.build(1L, request);

        assertTrue(result.text().contains("Documento: algebra.pdf"));
        assertEquals(50L, result.sources().get(0).documentId());
    }

    @Test
    void shouldBuildContextWithMultipleSources() {
        SemanticSearchRequest request = request(SourceSelectionMode.PREFERRED);

        RetrievedSource first = source(10L, null, null);
        RetrievedSource second = source(20L, null, null);

        when(semanticRetrievalService.retrieve(1L, request)).thenReturn(List.of(first, second));

        RagContext result = ragService.build(1L, request);

        assertEquals(2, result.sources().size());
        assertTrue(result.text().contains("Fonte 1"));
        assertTrue(result.text().contains("Fonte 2"));
    }

    @Test
    void shouldAllowEmptySourcesInPreferredMode() {
        SemanticSearchRequest request = request(SourceSelectionMode.PREFERRED);

        when(semanticRetrievalService.retrieve(1L, request)).thenReturn(List.of());

        RagContext result = ragService.build(1L, request);

        assertEquals("", result.text());
        assertTrue(result.sources().isEmpty());
    }

    @Test
    void shouldRejectEmptySourcesInExclusiveMode() {
        SemanticSearchRequest request = request(SourceSelectionMode.EXCLUSIVE);

        when(semanticRetrievalService.retrieve(1L, request)).thenReturn(List.of());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> ragService.build(1L, request)
        );

        assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, exception.getStatusCode());
    }

    @Test
    void shouldPreserveSourceFilters() {
        SemanticSearchRequest request = new SemanticSearchRequest(
                "Explique anéis", Set.of(1L), Set.of(2L), Set.of(10L), SourceSelectionMode.PREFERRED, 3, 0.8
        );

        when(semanticRetrievalService.retrieve(1L, request)).thenReturn(List.of());

        ragService.build(1L, request);

        verify(semanticRetrievalService).retrieve(1L, request);
    }

    @Test
    void shouldDefaultToPreferredModeWhenSelectionModeIsNull() {
        SemanticSearchRequest request = request(null);

        when(semanticRetrievalService.retrieve(1L, request)).thenReturn(List.of());

        RagContext result = ragService.build(1L, request);

        assertEquals("", result.text());
        assertTrue(result.sources().isEmpty());

        verify(semanticRetrievalService).retrieve(1L, request);
    }

    private SemanticSearchRequest request(SourceSelectionMode mode) {
        return new SemanticSearchRequest("Explique anéis", null, null, null, mode, null, null
        );
    }

    private RetrievedSource source(
            Long contentId,
            Long documentId,
            String documentName
    ) {
        return new RetrievedSource(
                "chunk-1",
                0,
                "Trecho sobre anéis",
                0.95,
                contentId,
                "Anéis",
                2L,
                "Álgebra",
                1L,
                "Matemática",
                documentId,
                documentName
        );
    }
}