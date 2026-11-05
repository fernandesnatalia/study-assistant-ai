package br.com.studyassistant.studyassistantai.controller;

import br.com.studyassistant.studyassistantai.model.Content;
import br.com.studyassistant.studyassistantai.service.ContentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

@RestController
@RequestMapping("/api/contents")
public class ContentController {

    private final ContentService contentService;

    public ContentController(ContentService contentService) {
        this.contentService = contentService;
    }

    @PostMapping
    public ResponseEntity<Content> createContent(
            @Valid @RequestBody Content content
    ) {
        Content result = contentService.createContent(content);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping
    public List<Content> getAllContents() {
        return contentService.getAllContents();
    }

    @GetMapping("/{id}")
    public Content getContentById(@PathVariable Long id) {
        return contentService.getContentById(id);
    }

    @PutMapping("/{id}")
    public Content updateContent(
            @PathVariable Long id,
            @RequestBody Content updatedContent
    ) {
        return contentService.updateContent(id, updatedContent);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteContent(@PathVariable Long id) {
        contentService.deleteContent(id);
    }
}