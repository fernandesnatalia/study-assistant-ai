package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.model.Content;
import br.com.studyassistant.studyassistantai.enums.ContentOrigin;
import br.com.studyassistant.studyassistantai.model.Document;
import br.com.studyassistant.studyassistantai.model.Topic;
import br.com.studyassistant.studyassistantai.model.User;
import br.com.studyassistant.studyassistantai.repository.ContentRepository;
import br.com.studyassistant.studyassistantai.repository.DocumentRepository;
import br.com.studyassistant.studyassistantai.repository.TopicRepository;
import br.com.studyassistant.studyassistantai.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.anyLong;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentExtractionService documentExtractionService;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private ContentRepository contentRepository;

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ContentVectorService contentVectorService;

    @InjectMocks
    private DocumentService documentService;

    private User createUser() {
        User user = new User();
        user.setId(1L);
        return user;
    }

    private Topic createTopic() {
        Topic topic = new Topic();
        topic.setId(1L);
        topic.setName("Teoria dos Anéis");
        return topic;
    }

    private MockMultipartFile createFile() {
        return new MockMultipartFile(
                "file", "material.txt", "text/plain", "Conteúdo original".getBytes(StandardCharsets.UTF_8)
        );
    }

    private void mockExistingUserAndTopic() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(createUser()));
        when(topicRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(createTopic()));
    }

    @Test
    void shouldConfirmDocumentAndCreateContent() {
        MockMultipartFile file = createFile();
        String extractedText = "Conteúdo original";

        mockExistingUserAndTopic();

        when(documentExtractionService.extractText(file))
                .thenReturn(extractedText);
        when(documentRepository.save(any(Document.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(contentRepository.save(any(Content.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Content result = documentService.confirmDocument(1L, file, 1L, "Título revisado", "Conteúdo revisado");

        assertNotNull(result);
        assertEquals("Título revisado", result.getTitle());
        assertEquals("Conteúdo revisado", result.getBody());
        assertEquals(ContentOrigin.DOCUMENT, result.getOrigin());

        assertEquals(1L, result.getUser().getId());
        assertEquals(1L, result.getTopic().getId());

        Document document = result.getDocument();

        assertNotNull(document);
        assertEquals("material.txt", document.getOriginalFilename());
        assertEquals("TXT", document.getFormat());
        assertEquals(file.getSize(), document.getSizeBytes());
        assertEquals(extractedText.length(), document.getExtractedCharacterCount());
        assertNotNull(document.getUploadedAt());
        assertNotNull(document.getProcessedAt());
        assertSame(result.getUser(), document.getUser());

        var inOrder = inOrder(documentRepository, contentRepository);

        inOrder.verify(documentRepository).save(document);
        inOrder.verify(contentRepository).save(result);
    }

    @Test
    void shouldRejectMissingUserId() {
        assertBadRequest(null, createFile(), 1L, "Título", "Conteúdo");

        verifyNoInteractions(
                userRepository, topicRepository, documentExtractionService, documentRepository, contentRepository
        );
    }

    @Test
    void shouldRejectMissingTopicId() {
        assertBadRequest(1L, createFile(), null, "Título", "Conteúdo");

        verifyNoInteractions(
                userRepository, topicRepository, documentExtractionService, documentRepository, contentRepository
        );
    }

    @Test
    void shouldRejectBlankTitle() {
        assertBadRequest(1L, createFile(), 1L, "   ", "Conteúdo");

        verifyNoInteractions(
                userRepository, topicRepository, documentExtractionService, documentRepository, contentRepository
        );
    }

    @Test
    void shouldRejectBlankBody() {
        assertBadRequest(1L, createFile(), 1L, "Título", "   ");

        verifyNoInteractions(
                userRepository, topicRepository, documentExtractionService, documentRepository, contentRepository
        );
    }

    @Test
    void shouldRejectNonexistentUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> documentService.confirmDocument(1L, createFile(), 1L, "Título", "Conteúdo")
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verifyNoInteractions(topicRepository, documentExtractionService, documentRepository, contentRepository);
    }

    @Test
    void shouldRejectTopicFromAnotherUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(createUser()));
        when(topicRepository.findByIdAndUserId(2L, 1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> documentService.confirmDocument(1L, createFile(), 2L, "Título", "Conteúdo")
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verifyNoInteractions(documentExtractionService, documentRepository, contentRepository);
    }

    @Test
    void shouldNotPersistWhenExtractionFails() {
        MockMultipartFile file = createFile();

        mockExistingUserAndTopic();

        when(documentExtractionService.extractText(file))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not extract document text"));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> documentService.confirmDocument(1L, file, 1L, "Título", "Conteúdo")
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        verifyNoInteractions(documentRepository, contentRepository);
    }

    @Test
    void shouldPropagateContentPersistenceFailure() {
        MockMultipartFile file = createFile();

        mockExistingUserAndTopic();

        when(documentExtractionService.extractText(file))
                .thenReturn("Conteúdo original");
        when(documentRepository.save(any(Document.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(contentRepository.save(any(Content.class)))
                .thenThrow(new IllegalStateException("Content persistence failed"));

        assertThrows(IllegalStateException.class,
                () -> documentService.confirmDocument(1L, file, 1L, "Título", "Conteúdo")
        );

        verify(documentRepository).save(any(Document.class));
        verify(contentRepository).save(any(Content.class));
    }

    private void assertBadRequest(Long userId, MultipartFile file, Long topicId, String title, String body) {

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> documentService.confirmDocument(userId, file, topicId, title, body)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }


    @Test
    void shouldDeleteDocumentAndAssociatedContents() {
        Document document = mock(Document.class);
        Content content = new Content();

        when(documentRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(document));
        when(contentRepository.findByDocumentIdAndUserId(10L, 1L)).thenReturn(List.of(content));

        documentService.deleteDocument(10L, 1L);

        InOrder inOrder = inOrder(contentRepository, documentRepository);

        inOrder.verify(contentRepository).deleteAll(List.of(content));
        inOrder.verify(contentRepository).flush();
        inOrder.verify(documentRepository).delete(document);
    }


    @Test
    void shouldRejectDeletingNonexistentDocument() {
        when(documentRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> documentService.deleteDocument(10L, 1L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verifyNoInteractions(contentRepository);
        verify(documentRepository, never()).delete(any(Document.class));
    }


    @Test
    void shouldRejectDeletingDocumentFromAnotherUser() {
        when(documentRepository.findByIdAndUserId(10L, 2L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> documentService.deleteDocument(10L, 2L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(documentRepository).findByIdAndUserId(10L, 2L);
        verifyNoInteractions(contentRepository);
        verify(documentRepository, never()).delete(any(Document.class));
    }

    @Test
    void shouldPreserveDocumentWithRemainingContents() {
        Document document = mock(Document.class);

        when(documentRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(document));
        when(contentRepository.countByDocumentId(10L)).thenReturn(1L);

        documentService.deleteOrphanDocuments(List.of(10L), 1L);

        verify(documentRepository, never()).delete(any(Document.class));
    }

    @Test
    void shouldDeleteOrphanDocument() {
        Document document = mock(Document.class);

        when(documentRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(document));
        when(contentRepository.countByDocumentId(10L)).thenReturn(0L);

        documentService.deleteOrphanDocuments(List.of(10L), 1L);

        verify(documentRepository).delete(document);
    }

    @Test
    void shouldNotDeleteOrphanDocumentFromAnotherUser() {
        when(documentRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.empty());

        documentService.deleteOrphanDocuments(List.of(10L), 1L);

        verify(contentRepository, never()).countByDocumentId(anyLong());
        verify(documentRepository, never()).delete(any(Document.class));
    }

    @Test
    void shouldHandleEmptyDocumentList() {
        documentService.deleteOrphanDocuments(List.of(), 1L);

        verifyNoInteractions(documentRepository, contentRepository);
    }
}