package br.com.studyassistant.studyassistantai.controller;

import br.com.studyassistant.studyassistantai.dto.ContentApprovalRequest;
import br.com.studyassistant.studyassistantai.dto.ContentGenerationRequest;
import br.com.studyassistant.studyassistantai.dto.GeneratedContentResponse;
import br.com.studyassistant.studyassistantai.model.Content;
import br.com.studyassistant.studyassistantai.model.Topic;
import br.com.studyassistant.studyassistantai.service.AuthenticatedUserService;
import br.com.studyassistant.studyassistantai.service.ContentAiService;
import br.com.studyassistant.studyassistantai.service.ContentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/contents")
public class ContentController {

    private final ContentService contentService;
    private final ContentAiService contentAiService;
    private final AuthenticatedUserService authenticatedUserService;

    public ContentController(
            ContentService contentService,
            ContentAiService contentAiService,
            AuthenticatedUserService authenticatedUserService
    ) {
        this.contentService = contentService;
        this.contentAiService = contentAiService;
        this.authenticatedUserService = authenticatedUserService;
    }

    @PostMapping
    public ResponseEntity<Content> createContent(
            @Valid @RequestBody Content content,
            Authentication authentication
    ) {
        Long userId = authenticatedUserService.getAuthenticatedUserId(authentication);
        Content result = contentService.approveGeneratedContent(userId, content);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping("/generate")
    public GeneratedContentResponse generateContent(
            @Valid @RequestBody ContentGenerationRequest request,
            Authentication authentication
    ) {
        Long userId = authenticatedUserService.getAuthenticatedUserId(authentication);
        return contentAiService.generateContent(userId, request);
    }

    @PostMapping("/approve")
    public ResponseEntity<Content> approveGeneratedContent(
            @Valid @RequestBody ContentApprovalRequest request,
            Authentication authentication
    ) {
        Long userId = authenticatedUserService.getAuthenticatedUserId(authentication);

        Topic topic = new Topic();
        topic.setId(request.topicId());

        Content content = new Content();
        content.setTitle(request.title());
        content.setBody(request.body());
        content.setTopic(topic);

        Content result = contentService.createContent(userId, content);

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping
    public List<Content> getAllContents(Authentication authentication) {
        Long userId = authenticatedUserService.getAuthenticatedUserId(authentication);
        return contentService.getAllContents(userId);
    }

    @GetMapping("/{id}")
    public Content getContentById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = authenticatedUserService.getAuthenticatedUserId(authentication);
        return contentService.getContentById(id, userId);
    }

    @PutMapping("/{id}")
    public Content updateContent(
            @PathVariable Long id,
            @RequestBody Content updatedContent,
            Authentication authentication
    ) {
        Long userId = authenticatedUserService.getAuthenticatedUserId(authentication);
        return contentService.updateContent(id, userId, updatedContent);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteContent(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = authenticatedUserService.getAuthenticatedUserId(authentication);
        contentService.deleteContent(id, userId);
    }
}