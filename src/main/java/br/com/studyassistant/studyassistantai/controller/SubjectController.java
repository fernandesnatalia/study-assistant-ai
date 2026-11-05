package br.com.studyassistant.studyassistantai.controller;

import br.com.studyassistant.studyassistantai.model.Subject;
import br.com.studyassistant.studyassistantai.service.SubjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @PostMapping
    public ResponseEntity<Subject> createSubject(@RequestBody Subject subject) {
        Subject result = subjectService.createSubject(subject);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);    }

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
            @RequestBody Subject updatedSubject
    ) {
        return subjectService.updateSubject(id, updatedSubject);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSubject(@PathVariable Long id){
        subjectService.deleteSubject(id);
    }
}
