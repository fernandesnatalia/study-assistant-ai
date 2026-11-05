package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.model.Content;
import br.com.studyassistant.studyassistantai.model.Topic;
import br.com.studyassistant.studyassistantai.repository.ContentRepository;
import br.com.studyassistant.studyassistantai.repository.TopicRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class ContentService {

    private final ContentRepository contentRepository;
    private final TopicRepository topicRepository;

    public ContentService(
            ContentRepository contentRepository,
            TopicRepository topicRepository
    ) {
        this.contentRepository = contentRepository;
        this.topicRepository = topicRepository;
    }

    public Content createContent(Content content) {
        if (content.getTopic().getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Topic ID is required"
            );
        }

        Topic topic = getExistingTopic(content.getTopic().getId());
        content.setTopic(topic);

        return contentRepository.save(content);
    }

    public List<Content> getAllContents() {
        return contentRepository.findAll();
    }

    public Content getContentById(Long id) {
        return contentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Content not found"
                ));
    }

    public Content updateContent(Long id, Content updatedContent) {
        Content content = getContentById(id);

        if (updatedContent.getTitle() == null ||
                updatedContent.getTitle().isBlank() ||
                updatedContent.getBody() == null ||
                updatedContent.getBody().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Title and body are required"
            );
        }

        content.setTitle(updatedContent.getTitle());
        content.setBody(updatedContent.getBody());

        return contentRepository.save(content);
    }

    public void deleteContent(Long id) {
        Content content = getContentById(id);
        contentRepository.delete(content);
    }

    private Topic getExistingTopic(Long topicId) {
        return topicRepository.findById(topicId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Topic not found"
                ));
    }
}