package br.com.studyassistant.studyassistantai.controller;

import br.com.studyassistant.studyassistantai.model.Subject;
import br.com.studyassistant.studyassistantai.model.Topic;
import br.com.studyassistant.studyassistantai.service.AuthenticatedUserService;
import br.com.studyassistant.studyassistantai.service.SubjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
import java.util.Set;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    private final SubjectService subjectService;
    private final AuthenticatedUserService authenticatedUserService;

    public SubjectController(
            SubjectService subjectService,
            AuthenticatedUserService authenticatedUserService
    ) {
        this.subjectService = subjectService;
        this.authenticatedUserService = authenticatedUserService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Subject createSubject(
            @Valid @RequestBody Subject subject,
            Authentication authentication
    ) {
        Long userId = authenticatedUserService.getAuthenticatedUserId(authentication);
        return subjectService.createSubject(subject, userId);
    }

    @GetMapping
    public List<Subject> getAllSubjects(Authentication authentication) {
        Long userId = authenticatedUserService.getAuthenticatedUserId(authentication);
        return subjectService.getAllSubjects(userId);
    }

    @GetMapping("/{id}")
    public Subject getSubjectById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = authenticatedUserService.getAuthenticatedUserId(authentication);
        return subjectService.getSubjectById(id, userId);
    }

    @PutMapping("/{id}")
    public Subject updateSubjectById(
            @PathVariable Long id,
            @Valid @RequestBody Subject updatedSubject,
            Authentication authentication
    ) {
        Long userId = authenticatedUserService.getAuthenticatedUserId(authentication);
        return subjectService.updateSubject(id, userId, updatedSubject);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSubject(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = authenticatedUserService.getAuthenticatedUserId(authentication);
        subjectService.deleteSubject(id, userId);
    }

    @GetMapping("/{subjectId}/topics")
    public Set<Topic> getSubjectTopics(
            @PathVariable Long subjectId,
            Authentication authentication
    ) {
        Long userId = authenticatedUserService.getAuthenticatedUserId(authentication);
        return subjectService.getSubjectTopics(subjectId, userId);
    }
}