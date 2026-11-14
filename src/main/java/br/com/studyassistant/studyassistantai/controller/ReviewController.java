package br.com.studyassistant.studyassistantai.controller;

import br.com.studyassistant.studyassistantai.dto.ReviewFlashcardResponse;
import br.com.studyassistant.studyassistantai.service.AuthenticatedUserService;
import br.com.studyassistant.studyassistantai.service.ReviewService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final AuthenticatedUserService authenticatedUserService;

    public ReviewController(
            ReviewService reviewService,
            AuthenticatedUserService authenticatedUserService
    ) {
        this.reviewService = reviewService;
        this.authenticatedUserService = authenticatedUserService;
    }

    @GetMapping("/pending")
    public List<ReviewFlashcardResponse> findPendingReviews(
            Authentication authentication
    ) {
        Long userId = authenticatedUserService.getAuthenticatedUserId(authentication);

        return reviewService.findPendingReviews(userId);
    }
}