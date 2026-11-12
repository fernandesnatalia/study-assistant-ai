package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.model.Subject;
import br.com.studyassistant.studyassistantai.model.Topic;
import br.com.studyassistant.studyassistantai.model.User;
import br.com.studyassistant.studyassistantai.repository.SubjectRepository;
import br.com.studyassistant.studyassistantai.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Set;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;

    public SubjectService(SubjectRepository subjectRepository, UserRepository userRepository) {
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
    }

    public Subject createSubject(Subject subject, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        subject.setUser(user);

        return subjectRepository.save(subject);
    }

    public List<Subject> getAllSubjects(Long userId) {
        return subjectRepository.findByUserId(userId);
    }

    public Subject getSubjectById(Long id, Long userId) {
        return subjectRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subject not found"));
    }

    public Subject updateSubject(Long id, Long userId, Subject updatedSubject) {
        Subject subject = getSubjectById(id, userId);
        subject.setName(updatedSubject.getName());
        return subjectRepository.save(subject);
    }

    @Transactional
    public void deleteSubject(Long id, Long userId) {
        Subject subject = getSubjectById(id, userId);
        subjectRepository.delete(subject);
    }

    @Transactional(readOnly = true)
    public Set<Topic> getSubjectTopics(Long subjectId, Long userId) {
        Subject subject = getSubjectById(subjectId, userId);
        return Set.copyOf(subject.getTopics());
    }
}