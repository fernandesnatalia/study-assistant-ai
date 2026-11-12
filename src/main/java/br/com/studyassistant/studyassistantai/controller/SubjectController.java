package br.com.studyassistant.studyassistantai.controller;

import br.com.studyassistant.studyassistantai.model.Subject;
import br.com.studyassistant.studyassistantai.model.Topic;
import br.com.studyassistant.studyassistantai.service.SubjectService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Subject createSubject(
            @Valid @RequestBody Subject subject,
            @RequestParam Long userId
    ) {
        return subjectService.createSubject(subject, userId);
    }

    @GetMapping
    public List<Subject> getAllSubjects(@RequestParam Long userId) {
        return subjectService.getAllSubjects(userId);
    }

    @GetMapping("/{id}")
    public Subject getSubjectById(
            @PathVariable Long id,
            @RequestParam Long userId
    ) {
        return subjectService.getSubjectById(id, userId);
    }

    @PutMapping("/{id}")
    public Subject updateSubjectById(
            @PathVariable Long id,
            @RequestParam Long userId,
            @Valid @RequestBody Subject updatedSubject
    ) {
        return subjectService.updateSubject(id, userId, updatedSubject);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSubject(
            @PathVariable Long id,
            @RequestParam Long userId
    ) {
        subjectService.deleteSubject(id, userId);
    }

    @GetMapping("/{subjectId}/topics")
    public Set<Topic> getSubjectTopics(
            @PathVariable Long subjectId,
            @RequestParam Long userId
    ) {
        return subjectService.getSubjectTopics(subjectId, userId);
    }
}