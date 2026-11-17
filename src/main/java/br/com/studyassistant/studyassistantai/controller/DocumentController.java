package br.com.studyassistant.studyassistantai.controller;

import br.com.studyassistant.studyassistantai.dto.DocumentPreviewResponse;
import br.com.studyassistant.studyassistantai.dto.DocumentResponse;
import br.com.studyassistant.studyassistantai.model.Content;
import br.com.studyassistant.studyassistantai.service.AuthenticatedUserService;
import br.com.studyassistant.studyassistantai.service.DocumentExtractionService;
import br.com.studyassistant.studyassistantai.service.DocumentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Locale;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentExtractionService documentExtractionService;
    private final AuthenticatedUserService authenticatedUserService;
    private final DocumentService documentService;

    public DocumentController(
            DocumentExtractionService documentExtractionService,
            AuthenticatedUserService authenticatedUserService,
            DocumentService documentService
    ) {
        this.documentExtractionService = documentExtractionService;
        this.authenticatedUserService = authenticatedUserService;
        this.documentService = documentService;
    }

    @PostMapping(
            value = "/preview",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public DocumentPreviewResponse previewDocument(
            @RequestPart("file") MultipartFile file,
            Authentication authentication
    ) {
        authenticatedUserService.getAuthenticatedUserId(authentication);

        String body = documentExtractionService.extractText(file);

        String filename = file.getOriginalFilename();

        String format = filename.toLowerCase(Locale.ROOT).endsWith(".pdf") ? "PDF" : "TXT";

        String title = filename.substring(0, filename.lastIndexOf('.'));

        return new DocumentPreviewResponse(filename, format, file.getSize(), title, body, body.length());
    }

    @PostMapping(
            value = "/confirm",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<DocumentResponse> confirmDocument(
            @RequestPart("file") MultipartFile file,
            @RequestParam("topicId") Long topicId,
            @RequestParam("title") String title,
            @RequestParam("body") String body,
            Authentication authentication
    ) {
        Long userId = authenticatedUserService.getAuthenticatedUserId(authentication);

        Content content = documentService.confirmDocument(userId, file, topicId, title, body);

        DocumentResponse response = new DocumentResponse(content.getDocument().getId(), content.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = authenticatedUserService.getAuthenticatedUserId(authentication);

        documentService.deleteDocument(id, userId);

        return ResponseEntity.noContent().build();
    }
}