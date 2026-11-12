package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.model.Subject;
import br.com.studyassistant.studyassistantai.model.Topic;
import br.com.studyassistant.studyassistantai.model.User;
import br.com.studyassistant.studyassistantai.repository.UserRepository;
import br.com.studyassistant.studyassistantai.repository.TopicRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class TopicService {

    private final TopicRepository topicRepository;

    private final UserRepository userRepository;

    public TopicService(TopicRepository topicRepository, UserRepository userRepository) {
        this.topicRepository = topicRepository;
        this.userRepository = userRepository;
    }

    public Topic createTopic(Topic topic, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        topic.setUser(user);
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

        for (Subject subject : topic.getSubjects()) {
            subject.getTopics().remove(topic);
        }

        topicRepository.delete(topic);
    }
}