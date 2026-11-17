package br.com.studyassistant.studyassistantai.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.io.IOException;
import java.io.InputStream;

class DocumentExtractionServiceTest {

    private final DocumentExtractionService documentExtractionService = new DocumentExtractionService();

    @Test
    void shouldExtractTextFromTxtFile() {
        MockMultipartFile file = createFile("material.txt", "text/plain", "Conteúdo de estudo sobre álgebra.");

        String result = documentExtractionService.extractText(file);

        assertEquals("Conteúdo de estudo sobre álgebra.", result);
    }

    @Test
    void shouldNormalizeExtractedText() {
        MockMultipartFile file = createFile(
                "material.txt", "text/plain", "  Primeiro   parágrafo.\r\n\r\n\r\nSegundo\t parágrafo.  "
        );

        String result = documentExtractionService.extractText(file);

        assertEquals("Primeiro parágrafo.\n\nSegundo parágrafo.", result);
    }

    @Test
    void shouldExtractTextFromPdfFile() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream("/documents/document-extraction-test.pdf")
        ) {
            assertNotNull(inputStream);

            MockMultipartFile file = new MockMultipartFile(
                    "file", "document-extraction-test.pdf", "application/pdf", inputStream
            );

            String result = documentExtractionService.extractText(file);

            assertTrue(result.contains("Study Assistant AI"));
            assertTrue(result.contains("Document with extractable text."));
        }
    }

    @Test
    void shouldRejectTextExceedingExtractionLimit() {
        String content = "A".repeat(500_001);

        MockMultipartFile file = createFile("material.txt", "text/plain", content);

        assertBadRequest(file);
    }

    @Test
    void shouldRejectMissingFile() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> documentExtractionService.extractText(null)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void shouldRejectEmptyFile() {
        MockMultipartFile file = createFile("material.txt", "text/plain", "");

        assertBadRequest(file);
    }

    @Test
    void shouldRejectUnsupportedExtension() {
        MockMultipartFile file = createFile(
                "material.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "Conteúdo de estudo"
        );

        assertBadRequest(file);
    }

    @Test
    void shouldRejectMissingFilename() {
        MockMultipartFile file = createFile("", "text/plain", "Conteúdo de estudo");

        assertBadRequest(file);
    }

    @Test
    void shouldRejectFileExceedingSizeLimit() {
        MultipartFile file = mock(MultipartFile.class);

        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(10L * 1024 * 1024 + 1);

        assertBadRequest(file);
    }

    @Test
    void shouldRejectFileWithMismatchedExtension() {
        MockMultipartFile file = createFile(
                "material.pdf", "application/pdf", "Este arquivo não contém uma estrutura PDF."
        );

        assertBadRequest(file);
    }

    @Test
    void shouldRejectTextWithoutExtractableContent() {
        MockMultipartFile file = createFile("material.txt", "text/plain", "   \n\t   ");

        assertBadRequest(file);
    }

    private MockMultipartFile createFile(
            String filename,
            String contentType,
            String content
    ) {
        return new MockMultipartFile("file", filename, contentType, content.getBytes(StandardCharsets.UTF_8));
    }

    private void assertBadRequest(MultipartFile file) {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> documentExtractionService.extractText(file)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }
}