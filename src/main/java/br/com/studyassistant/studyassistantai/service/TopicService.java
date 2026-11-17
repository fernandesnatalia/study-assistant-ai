package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.model.Subject;
import br.com.studyassistant.studyassistantai.model.Topic;
import br.com.studyassistant.studyassistantai.model.User;
import br.com.studyassistant.studyassistantai.repository.DocumentRepository;
import br.com.studyassistant.studyassistantai.repository.UserRepository;
import br.com.studyassistant.studyassistantai.repository.TopicRepository;
import br.com.studyassistant.studyassistantai.repository.SubjectRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class TopicService {

    private final TopicRepository topicRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private final DocumentService documentService;

    public TopicService(
            TopicRepository topicRepository,
            SubjectRepository subjectRepository,
            UserRepository userRepository,
            DocumentRepository documentRepository,
            DocumentService documentService
    ) {
        this.topicRepository = topicRepository;
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
        this.documentRepository = documentRepository;
        this.documentService = documentService;
    }

    public Topic createTopic(Topic topic, Long userId, Long subjectId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Subject subject = subjectRepository.findByIdAndUserId(subjectId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subject not found"));

        topic.setUser(user);
        topic.setSubject(subject);

        return topicRepository.save(topic);
    }

    public List<Topic> getAllTopics(Long userId) {
        return topicRepository.findByUserId(userId);
    }

    public Topic getTopicById(Long id, Long userId) {
        return topicRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Topic not found"));
    }

    public Topic updateTopic(Long id, Long userId, Topic updatedTopic) {
        Topic topic = getTopicById(id, userId);
        topic.setName(updatedTopic.getName());
        return topicRepository.save(topic);
    }

    @Transactional
    public void deleteTopic(Long id, Long userId) {
        Topic topic = getTopicById(id, userId);

        List<Long> documentIds = documentRepository.findDocumentIdsByTopicIdAndUserId(id, userId);

        topicRepository.delete(topic);
        topicRepository.flush();
        documentService.deleteOrphanDocuments(documentIds, userId);
    }
}