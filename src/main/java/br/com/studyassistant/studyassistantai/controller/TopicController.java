package br.com.studyassistant.studyassistantai.controller;

import br.com.studyassistant.studyassistantai.model.Topic;
import br.com.studyassistant.studyassistantai.service.AuthenticatedUserService;
import br.com.studyassistant.studyassistantai.service.TopicService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/topics")
public class TopicController {

    private final TopicService topicService;
    private final AuthenticatedUserService authenticatedUserService;

    public TopicController(
            TopicService topicService,
            AuthenticatedUserService authenticatedUserService
    ) {
        this.topicService = topicService;
        this.authenticatedUserService = authenticatedUserService;
    }

    @PostMapping
    public ResponseEntity<Topic> createTopic(
            @Valid @RequestBody Topic topic,
            @RequestParam Long subjectId,
            Authentication authentication
    ) {
        Long userId = authenticatedUserService.getAuthenticatedUserId(authentication);
        Topic result = topicService.createTopic(topic, userId, subjectId);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping
    public List<Topic> getAllTopics(Authentication authentication) {
        Long userId = authenticatedUserService.getAuthenticatedUserId(authentication);
        return topicService.getAllTopics(userId);
    }

    @GetMapping("/{id}")
    public Topic getTopicById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = authenticatedUserService.getAuthenticatedUserId(authentication);
        return topicService.getTopicById(id, userId);
    }

    @PutMapping("/{id}")
    public Topic updateTopic(
            @PathVariable Long id,
            @Valid @RequestBody Topic updatedTopic,
            Authentication authentication
    ) {
        Long userId = authenticatedUserService.getAuthenticatedUserId(authentication);
        return topicService.updateTopic(id, userId, updatedTopic);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTopic(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = authenticatedUserService.getAuthenticatedUserId(authentication);
        topicService.deleteTopic(id, userId);
    }
}