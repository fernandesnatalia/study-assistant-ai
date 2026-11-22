package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.model.Content;
import br.com.studyassistant.studyassistantai.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;
import static br.com.studyassistant.studyassistantai.constants.SqlQueries.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;

class ContentVectorServiceTest {

    private VectorStore vectorStore;
    private ContentChunkingService chunkingService;
    private JdbcTemplate jdbcTemplate;
    private ContentVectorService service;

    @BeforeEach
    void setUp() {
        vectorStore = mock(VectorStore.class);
        chunkingService = mock(ContentChunkingService.class);
        jdbcTemplate = mock(JdbcTemplate.class);
        service = new ContentVectorService(vectorStore, chunkingService, jdbcTemplate);
    }

    @Test
    void shouldIndexChunksWithCorrectMetadata() {
        Content content = createContent();

        when(chunkingService.split("Conteúdo de estudo")).thenReturn(List.of("Primeiro trecho", "Segundo trecho"));

        service.indexContent(content);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Document>> captor = ArgumentCaptor.forClass(List.class);

        verify(vectorStore).add(captor.capture());

        List<Document> documents = captor.getValue();

        assertEquals(2, documents.size());

        for (int i = 0; i < documents.size(); i++) {
            Document document = documents.get(i);

            assertEquals(10L, document.getMetadata().get("contentId"));
            assertEquals(20L, document.getMetadata().get("userId"));
            assertEquals(i, document.getMetadata().get("chunkIndex"));
            assertNotNull(document.getId());
        }

        assertEquals("Primeiro trecho", documents.get(0).getText());
        assertEquals("Segundo trecho", documents.get(1).getText());

        assertNotEquals(documents.get(0).getId(), documents.get(1).getId());
    }

    @Test
    void shouldDeletePreviousVectorsBeforeIndexing() {
        Content content = createContent();

        when(chunkingService.split(anyString())).thenReturn(List.of("Trecho de estudo"));
        when(jdbcTemplate.queryForList(eq(FIND_CONTENT_VECTORS), eq(String.class), eq("10"), eq("20")))
                .thenReturn(List.of("previous-vector-id"));

        service.indexContent(content);

        InOrder inOrder = inOrder(jdbcTemplate, vectorStore);

        inOrder.verify(jdbcTemplate).queryForList(eq(FIND_CONTENT_VECTORS), eq(String.class), eq("10"), eq("20"));

        inOrder.verify(vectorStore).add(anyList());
        inOrder.verify(vectorStore).delete(List.of("previous-vector-id"));
    }

    @Test
    void shouldPreservePreviousVectorsWhenIndexingFails() {
        Content content = createContent();

        when(chunkingService.split(anyString())).thenReturn(List.of("Trecho de estudo"));
        when(jdbcTemplate.queryForList(eq(FIND_CONTENT_VECTORS), eq(String.class), eq("10"), eq("20")))
                .thenReturn(List.of("previous-vector-id"));

        doThrow(new RuntimeException("Embedding generation failed")).when(vectorStore).add(anyList());

        assertThrows(RuntimeException.class, () -> service.indexContent(content));

        verify(vectorStore, never()).delete(anyList());
    }

    @Test
    void shouldDeleteVectorsByContentAndUser() {
        service.deleteContentVectors(10L, 20L);

        verify(jdbcTemplate).update(eq(DELETE_CONTENT_VECTORS), eq("10"), eq("20"));
    }

    @Test
    void shouldDeleteAllUserVectors() {
        service.deleteUserVectors(20L);

        verify(jdbcTemplate).update(eq(DELETE_USER_VECTORS), eq("20"));
    }

    @Test
    void shouldRejectContentWithoutId() {
        Content content = createContent();
        content.setId(null);

        assertThrows(IllegalArgumentException.class, () -> service.indexContent(content));

        verifyNoInteractions(vectorStore, jdbcTemplate, chunkingService);
    }

    private Content createContent() {
        User user = new User();
        user.setId(20L);

        Content content = new Content();
        content.setId(10L);
        content.setUser(user);
        content.setBody("Conteúdo de estudo");

        return content;
    }
}