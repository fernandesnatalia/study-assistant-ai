package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.model.Subject;
import br.com.studyassistant.studyassistantai.repository.SubjectRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;

    public Subject createSubject(Subject subject) {
        return subjectRepository.save(subject);
    }

    public SubjectService(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }

    public Subject getSubjectById(Long id) {
        return subjectRepository.findById(id)
                .orElseThrow(()
                        -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subject not found")
                );
    }

    public Subject updateSubject(Long id, Subject updatedSubject) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Subject not found"
                ));

        subject.setName(updatedSubject.getName());

        return subjectRepository.save(subject);
    }

    public void deleteSubject(Long id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Subject not found"
                ));

        subjectRepository.delete(subject);
    }
}
