package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.model.Content;
import br.com.studyassistant.studyassistantai.model.Topic;
import br.com.studyassistant.studyassistantai.model.User;
import br.com.studyassistant.studyassistantai.repository.ContentRepository;
import br.com.studyassistant.studyassistantai.repository.TopicRepository;
import br.com.studyassistant.studyassistantai.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class ContentService {

    private final ContentRepository contentRepository;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;

    public ContentService(
            ContentRepository contentRepository,
            TopicRepository topicRepository,
            UserRepository userRepository
    ) {
        this.contentRepository = contentRepository;
        this.topicRepository = topicRepository;
        this.userRepository = userRepository;
    }

    public Content createContent(Long userId, Content content) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User ID is required");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (content.getTopic() == null || content.getTopic().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Topic ID is required");
        }

        Topic topic = getExistingTopic(content.getTopic().getId(), userId);

        content.setId(null);
        content.setUser(user);
        content.setTopic(topic);

        return contentRepository.save(content);
    }

    public List<Content> getAllContents(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return contentRepository.findByUserId(userId);
    }

    public Content getContentById(Long id, Long userId) {
        return contentRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Content not found"));
    }

    public Content updateContent(Long id, Long userId, Content updatedContent) {
        Content content = getContentById(id, userId);

        if (updatedContent.getTitle() == null ||
                updatedContent.getTitle().isBlank() ||
                updatedContent.getBody() == null ||
                updatedContent.getBody().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title and body are required");
        }

        content.setTitle(updatedContent.getTitle());
        content.setBody(updatedContent.getBody());

        return contentRepository.save(content);
    }

    public void deleteContent(Long id, Long userId) {
        Content content = getContentById(id, userId);
        contentRepository.delete(content);
    }

    private Topic getExistingTopic(Long topicId, Long userId) {
        return topicRepository.findByIdAndUserId(topicId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Topic not found"));
    }
}