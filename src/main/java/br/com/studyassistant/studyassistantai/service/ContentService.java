package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.model.Content;
import br.com.studyassistant.studyassistantai.model.ContentOrigin;
import br.com.studyassistant.studyassistantai.model.Document;
import br.com.studyassistant.studyassistantai.model.Topic;
import br.com.studyassistant.studyassistantai.model.User;
import br.com.studyassistant.studyassistantai.repository.ContentRepository;
import br.com.studyassistant.studyassistantai.repository.DocumentRepository;
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
    private final DocumentRepository documentRepository;

    public ContentService(
            ContentRepository contentRepository,
            TopicRepository topicRepository,
            UserRepository userRepository,
            DocumentRepository documentRepository
    ) {
        this.contentRepository = contentRepository;
        this.topicRepository = topicRepository;
        this.userRepository = userRepository;
        this.documentRepository = documentRepository;
    }

    public Content createContent(Long userId, Content content) {
        return saveContent(userId, content, ContentOrigin.MANUAL);
    }

    public Content approveGeneratedContent(Long userId, Content content) {
        return saveContent(userId, content, ContentOrigin.AI_GENERATED);
    }

    private Content saveContent(Long userId, Content content, ContentOrigin origin) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User ID is required");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (content.getTopic() == null || content.getTopic().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Topic ID is required");
        }

        Topic topic = getExistingTopic(content.getTopic().getId(), userId);
        Document document = validateDocumentOrigin(userId, origin, content.getDocument());

        content.setId(null);
        content.setUser(user);
        content.setTopic(topic);
        content.setOrigin(origin);
        content.setDocument(document);

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

    private Document validateDocumentOrigin(
            Long userId,
            ContentOrigin origin,
            Document document
    ) {
        if (origin != ContentOrigin.DOCUMENT) {
            if (document != null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Only document content can reference a document"
                );
            }

            return null;
        }

        if (document == null || document.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Document ID is required");
        }

        return documentRepository.findByIdAndUserId(document.getId(), userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));
    }
}