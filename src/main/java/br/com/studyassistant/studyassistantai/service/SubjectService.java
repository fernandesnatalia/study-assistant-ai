package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.model.Subject;
import br.com.studyassistant.studyassistantai.model.Topic;
import br.com.studyassistant.studyassistantai.repository.SubjectRepository;
import br.com.studyassistant.studyassistantai.repository.TopicRepository;
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

    public SubjectService(
            SubjectRepository subjectRepository,
            TopicRepository topicRepository
    ) {
        this.subjectRepository = subjectRepository;
        this.topicRepository = topicRepository;
    }

    public Subject createSubject(Subject subject) {
        return subjectRepository.save(subject);
    }

    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }

    public Subject getSubjectById(Long id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Subject not found"
                ));
    }

    public Subject updateSubject(Long id, Subject updatedSubject) {
        Subject subject = getSubjectById(id);
        subject.setName(updatedSubject.getName());
        return subjectRepository.save(subject);
    }

    @Transactional
    public void deleteSubject(Long id) {
        Subject subject = getSubjectById(id);
        subjectRepository.delete(subject);
    }

    @Transactional
    public void addTopicToSubject(Long subjectId, Long topicId) {
        Subject subject = getSubjectById(subjectId);

        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Topic not found"
                ));

        subject.getTopics().add(topic);
    }

    @Transactional(readOnly = true)
    public Set<Topic> getSubjectTopics(Long subjectId) {
        Subject subject = getSubjectById(subjectId);
        return Set.copyOf(subject.getTopics());
    }

    @Transactional
    public void removeTopicFromSubject(Long subjectId, Long topicId) {
        Subject subject = getSubjectById(subjectId);

        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Topic not found"
                ));

        subject.getTopics().remove(topic);
    }
}