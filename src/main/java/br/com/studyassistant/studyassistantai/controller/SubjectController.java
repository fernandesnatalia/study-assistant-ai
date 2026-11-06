package br.com.studyassistant.studyassistantai.controller;

import br.com.studyassistant.studyassistantai.model.Subject;
import br.com.studyassistant.studyassistantai.model.Topic;
import br.com.studyassistant.studyassistantai.model.User;
import br.com.studyassistant.studyassistantai.service.SubjectService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
            @RequestParam Long userId,
            @Valid @RequestBody Subject subject
    ) {
        User user = new User();
        user.setId(userId);

        subject.setUser(user);

        return subjectService.createSubject(subject);
    }

    @GetMapping
    public List<Subject> getAllSubjects() {
        return subjectService.getAllSubjects();
    }

    @GetMapping("/{id}")
    public Subject getSubjectById(@PathVariable Long id) {
        return subjectService.getSubjectById(id);
    }

    @PutMapping("/{id}")
    public Subject updateSubjectById(
            @PathVariable Long id,
            @Valid @RequestBody Subject updatedSubject
    ) {
        return subjectService.updateSubject(id, updatedSubject);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSubject(@PathVariable Long id) {
        subjectService.deleteSubject(id);
    }

    @PutMapping("/{subjectId}/topics/{topicId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addTopicToSubject(
            @PathVariable Long subjectId,
            @PathVariable Long topicId
    ) {
        subjectService.addTopicToSubject(subjectId, topicId);
    }

    @GetMapping("/{subjectId}/topics")
    public Set<Topic> getSubjectTopics(@PathVariable Long subjectId) {
        return subjectService.getSubjectTopics(subjectId);
    }

    @DeleteMapping("/{subjectId}/topics/{topicId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeTopicFromSubject(
            @PathVariable Long subjectId,
            @PathVariable Long topicId
    ) {
        subjectService.removeTopicFromSubject(subjectId, topicId);
    }
}