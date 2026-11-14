package br.com.studyassistant.studyassistantai.controller;

import br.com.studyassistant.studyassistantai.dto.AnswerAttemptRequest;
import br.com.studyassistant.studyassistantai.dto.AnswerAttemptResponse;
import br.com.studyassistant.studyassistantai.service.AnswerAttemptService;
import br.com.studyassistant.studyassistantai.service.AuthenticatedUserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/answer-attempts")
public class AnswerAttemptController {

    private final AnswerAttemptService answerAttemptService;
    private final AuthenticatedUserService authenticatedUserService;

    public AnswerAttemptController(
            AnswerAttemptService answerAttemptService,
            AuthenticatedUserService authenticatedUserService
    ) {
        this.answerAttemptService = answerAttemptService;
        this.authenticatedUserService = authenticatedUserService;
    }

    @PostMapping
    public ResponseEntity<AnswerAttemptResponse> answer(
            @RequestParam Long flashcardId,
            @Valid @RequestBody AnswerAttemptRequest request,
            Authentication authentication
    ) {
        Long userId = authenticatedUserService.getAuthenticatedUserId(authentication);

        AnswerAttemptResponse response = answerAttemptService.answer(
                userId,
                flashcardId,
                request.answer()
        );

        return ResponseEntity
                .created(URI.create("/api/answer-attempts/" + response.id()))
                .body(response);
    }

    @PostMapping("/give-up")
    public ResponseEntity<AnswerAttemptResponse> giveUp(
            @RequestParam Long flashcardId,
            Authentication authentication
    ) {
        Long userId = authenticatedUserService.getAuthenticatedUserId(authentication);

        AnswerAttemptResponse response = answerAttemptService.giveUp(userId, flashcardId);

        return ResponseEntity
                .created(URI.create("/api/answer-attempts/" + response.id()))
                .body(response);
    }

    @GetMapping
    public List<AnswerAttemptResponse> findByUser(Authentication authentication) {
        Long userId = authenticatedUserService.getAuthenticatedUserId(authentication);
        return answerAttemptService.findByUser(userId);
    }

    @GetMapping("/flashcards/{flashcardId}")
    public List<AnswerAttemptResponse> findByUserAndFlashcard(
            @PathVariable Long flashcardId,
            Authentication authentication
    ) {
        Long userId = authenticatedUserService.getAuthenticatedUserId(authentication);
        return answerAttemptService.findByUserAndFlashcard(userId, flashcardId);
    }
}