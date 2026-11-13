package br.com.studyassistant.studyassistantai.controller;

import br.com.studyassistant.studyassistantai.dto.AnswerAttemptResponse;
import br.com.studyassistant.studyassistantai.service.AnswerAttemptService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @PostMapping("/give-up")
    public ResponseEntity<AnswerAttemptResponse> giveUp(
            @RequestParam Long userId,
            @RequestParam Long flashcardId
    ) {
        AnswerAttemptResponse response = answerAttemptService.giveUp(
                userId,
                flashcardId
        );

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