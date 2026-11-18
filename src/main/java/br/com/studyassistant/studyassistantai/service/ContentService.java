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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class ContentService {

    private final ContentRepository contentRepository;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private final ContentVectorService contentVectorService;

    public ContentService(
            ContentRepository contentRepository,
            TopicRepository topicRepository,
            UserRepository userRepository,
            DocumentRepository documentRepository,
            ContentVectorService contentVectorService
    ) {
        this.contentRepository = contentRepository;
        this.topicRepository = topicRepository;
        this.userRepository = userRepository;
        this.documentRepository = documentRepository;
        this.contentVectorService = contentVectorService;
    }

    @Transactional
    public Content createContent(Long userId, Content content) {
        return saveContent(userId, content, ContentOrigin.MANUAL);
    }

    @Transactional
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

        Content savedContent = contentRepository.save(content);

        contentVectorService.indexContent(savedContent);

        return savedContent;
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

    @Transactional
    public Content updateContent(Long id, Long userId, Content updatedContent) {
        Content content = getContentById(id, userId);

        if (updatedContent.getTitle() == null ||
                updatedContent.getTitle().isBlank() ||
                updatedContent.getBody() == null ||
                updatedContent.getBody().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title and body are required");
        }

        boolean bodyChanged = !content.getBody().equals(updatedContent.getBody());

        content.setTitle(updatedContent.getTitle());
        content.setBody(updatedContent.getBody());

        Content savedContent = contentRepository.save(content);

        if (bodyChanged) {
            contentVectorService.indexContent(savedContent);
        }

        return savedContent;
    }

    @Transactional
    public void deleteContent(Long id, Long userId) {
        Content content = getContentById(id, userId);

        Document document = content.getDocument();

        contentVectorService.deleteContentVectors(id, userId);
        contentRepository.delete(content);
        contentRepository.flush();

        if (document == null) {
            return;
        }

        if (contentRepository.countByDocumentId(document.getId()) == 0) {
            documentRepository.delete(document);
        }
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