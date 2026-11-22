package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.dto.RetrievedSource;
import br.com.studyassistant.studyassistantai.dto.SemanticSearchRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verifyNoInteractions;

class SemanticRetrievalServiceTest {

    private EmbeddingModel embeddingModel;
    private JdbcTemplate jdbcTemplate;
    private SourceSelectionService sourceSelectionService;
    private SemanticRetrievalService service;

    @BeforeEach
    void setUp() {
        embeddingModel = mock(EmbeddingModel.class);
        jdbcTemplate = mock(JdbcTemplate.class);
        sourceSelectionService = mock(SourceSelectionService.class);
        service = new SemanticRetrievalService(embeddingModel, jdbcTemplate, sourceSelectionService);
    }

    @Test
    void shouldSearchWithDefaultValues() {
        when(embeddingModel.embed("O que é um anel?")).thenReturn(new float[]{0.1f, 0.2f, 0.3f});

        SemanticSearchRequest request = new SemanticSearchRequest(
                "O que é um anel?",
                null, null, null, null, null, null
        );

        service.retrieve(1L, request);

        verify(sourceSelectionService).validate(1L, null, null, null);
        verify(embeddingModel).embed("O que é um anel?");

        Object[] parameters = captureParameters();

        assertEquals("[0.1,0.2,0.3]", parameters[0]);
        assertEquals(1L, parameters[1]);
        assertEquals(1L, parameters[2]);
        assertEquals(1L, parameters[3]);
        assertEquals(1L, parameters[4]);
        assertEquals(0.7, parameters[6]);
        assertEquals(5, parameters[7]);
    }

    @Test
    void shouldApplySelectedSourceFilters() {
        when(embeddingModel.embed(anyString())).thenReturn(new float[]{0.1f, 0.2f});

        SemanticSearchRequest request = new SemanticSearchRequest(
                "Explique anéis", Set.of(10L), Set.of(20L), Set.of(30L), null, 3, 0.8
        );

        service.retrieve(1L, request);

        verify(sourceSelectionService).validate(1L, Set.of(10L), Set.of(20L), Set.of(30L));

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);

        verify(jdbcTemplate).query(sqlCaptor.capture(), any(RowMapper.class), any(Object[].class));

        String sql = sqlCaptor.getValue();

        assertTrue(sql.contains("s.id IN (?)"));
        assertTrue(sql.contains("t.id IN (?)"));
        assertTrue(sql.contains("c.id IN (?)"));

        Object[] parameters = captureParameters();

        assertEquals(10L, parameters[5]);
        assertEquals(20L, parameters[6]);
        assertEquals(30L, parameters[7]);
        assertEquals(0.8, parameters[9]);
        assertEquals(3, parameters[10]);
    }

    @Test
    void shouldRejectEmptyQuery() {
        SemanticSearchRequest request = new SemanticSearchRequest(
                " ", null, null, null, null, null, null
        );

        assertThrows(IllegalArgumentException.class, () -> service.retrieve(1L, request));

        verifyNoInteractions(embeddingModel, jdbcTemplate);
    }

    @Test
    void shouldRejectInvalidTopK() {
        SemanticSearchRequest request = new SemanticSearchRequest(
                "Pergunta", null, null, null, null, 0, null
        );

        assertThrows(IllegalArgumentException.class, () -> service.retrieve(1L, request));

        verifyNoInteractions(embeddingModel, jdbcTemplate);
    }

    @Test
    void shouldRejectInvalidThreshold() {
        SemanticSearchRequest request = new SemanticSearchRequest(
                "Pergunta", null, null, null, null, 5, 1.5
        );

        assertThrows(IllegalArgumentException.class, () -> service.retrieve(1L, request));

        verifyNoInteractions(embeddingModel, jdbcTemplate);
    }

    @Test
    void shouldRejectMissingUserId() {
        SemanticSearchRequest request = new SemanticSearchRequest(
                "Pergunta", null, null, null, null, null, null
        );

        assertThrows(IllegalArgumentException.class, () -> service.retrieve(null, request));

        verifyNoInteractions(embeddingModel, jdbcTemplate);
    }

    @Test
    void shouldReturnEmptyListWhenNoSourcesAreFound() {
        when(embeddingModel.embed(anyString())).thenReturn(new float[]{0.1f, 0.2f});

        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any(Object[].class))).thenReturn(List.of());

        SemanticSearchRequest request = new SemanticSearchRequest(
                "Pergunta", null, null, null, null, null, null
        );

        List<RetrievedSource> results = service.retrieve(1L, request);

        assertTrue(results.isEmpty());
    }

    @Test
    void shouldPropagateDatabaseErrors() {
        when(embeddingModel.embed(anyString())).thenReturn(new float[]{0.1f, 0.2f});

        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any(Object[].class)))
                .thenThrow(new IllegalStateException("Database unavailable"));

        SemanticSearchRequest request = new SemanticSearchRequest(
                "Pergunta", null, null, null, null, null, null
        );

        assertThrows(IllegalStateException.class, () -> service.retrieve(1L, request));
    }

    private Object[] captureParameters() {
        ArgumentCaptor<Object[]> captor = ArgumentCaptor.forClass(Object[].class);

        verify(jdbcTemplate).query(anyString(), any(RowMapper.class), captor.capture());

        return captor.getValue();
    }
}