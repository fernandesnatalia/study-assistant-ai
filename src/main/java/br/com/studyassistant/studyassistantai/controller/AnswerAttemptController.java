package br.com.studyassistant.studyassistantai.controller;

import br.com.studyassistant.studyassistantai.dto.AnswerAttemptRequest;
import br.com.studyassistant.studyassistantai.dto.AnswerAttemptResponse;
import br.com.studyassistant.studyassistantai.service.AnswerAttemptService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/answer-attempts")
public class AnswerAttemptController {

    private final AnswerAttemptService answerAttemptService;

    public AnswerAttemptController(AnswerAttemptService answerAttemptService) {
        this.answerAttemptService = answerAttemptService;
    }

    @PostMapping
    public ResponseEntity<AnswerAttemptResponse> create(
            @Valid @RequestBody AnswerAttemptRequest request
    ) {
        AnswerAttemptResponse response = answerAttemptService.create(request);

        return ResponseEntity
                .created(URI.create("/api/answer-attempts/" + response.id()))
                .body(response);
    }

    @GetMapping("/users/{userId}")
    public List<AnswerAttemptResponse> findByUser(
            @PathVariable Long userId
    ) {
        return answerAttemptService.findByUser(userId);
    }

    @GetMapping("/users/{userId}/flashcards/{flashcardId}")
    public List<AnswerAttemptResponse> findByUserAndFlashcard(
            @PathVariable Long userId,
            @PathVariable Long flashcardId
    ) {
        return answerAttemptService.findByUserAndFlashcard(userId, flashcardId);
    }
}