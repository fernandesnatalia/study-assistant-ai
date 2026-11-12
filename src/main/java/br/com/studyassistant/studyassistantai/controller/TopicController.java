package br.com.studyassistant.studyassistantai.controller;

import br.com.studyassistant.studyassistantai.model.Topic;
import br.com.studyassistant.studyassistantai.service.TopicService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
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
@RequestMapping("/api/topics")
public class TopicController {

    private final TopicService topicService;

    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }

    @PostMapping
    public ResponseEntity<Topic> createTopic(
            @Valid @RequestBody Topic topic,
            @RequestParam Long userId
    ) {
        Topic result = topicService.createTopic(topic, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping
    public List<Topic> getAllTopics(@RequestParam Long userId) {
        return topicService.getAllTopics(userId);
    }

    @GetMapping("/{id}")
    public Topic getTopicById(
            @PathVariable Long id,
            @RequestParam Long userId
    ) {
        return topicService.getTopicById(id, userId);
    }

    @PutMapping("/{id}")
    public Topic updateTopic(
            @PathVariable Long id,
            @RequestParam Long userId,
            @Valid @RequestBody Topic updatedTopic
    ) {
        return topicService.updateTopic(id, userId, updatedTopic);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTopic(
            @PathVariable Long id,
            @RequestParam Long userId
    ) {
        topicService.deleteTopic(id, userId);
    }
}