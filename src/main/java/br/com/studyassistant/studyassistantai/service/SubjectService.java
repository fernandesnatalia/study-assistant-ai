package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.model.Subject;
import br.com.studyassistant.studyassistantai.model.Topic;
import br.com.studyassistant.studyassistantai.model.User;
import br.com.studyassistant.studyassistantai.repository.SubjectRepository;
import br.com.studyassistant.studyassistantai.repository.TopicRepository;
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
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;

    public SubjectService(
            SubjectRepository subjectRepository,
            TopicRepository topicRepository, UserRepository userRepository
    ) {
        this.subjectRepository = subjectRepository;
        this.topicRepository = topicRepository;
        this.userRepository = userRepository;
    }

    public Subject createSubject(Subject subject) {
        if (subject.getUser() == null || subject.getUser().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User ID is required");
        }

        Long userId = subject.getUser().getId();
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

    @Transactional
    public void addTopicToSubject(Long subjectId, Long topicId, Long userId) {
        Subject subject = getSubjectById(subjectId, userId);

        Topic topic = topicRepository.findByIdAndUserId(topicId, subject.getUser().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Topic not found"));

        subject.getTopics().add(topic);
    }

    @Transactional(readOnly = true)
    public Set<Topic> getSubjectTopics(Long subjectId, Long userId) {
        Subject subject = getSubjectById(subjectId, userId);
        return Set.copyOf(subject.getTopics());
    }

    @Transactional
    public void removeTopicFromSubject(Long subjectId, Long topicId, Long userId) {
        Subject subject = getSubjectById(subjectId, userId);

        Topic topic = topicRepository.findByIdAndUserId(topicId, subject.getUser().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Topic not found"));

        subject.getTopics().remove(topic);
    }
}