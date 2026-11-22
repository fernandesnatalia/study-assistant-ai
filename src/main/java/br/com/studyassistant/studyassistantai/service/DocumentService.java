package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.model.Content;
import br.com.studyassistant.studyassistantai.enums.ContentOrigin;
import br.com.studyassistant.studyassistantai.model.Document;
import br.com.studyassistant.studyassistantai.model.Topic;
import br.com.studyassistant.studyassistantai.model.User;
import br.com.studyassistant.studyassistantai.repository.ContentRepository;
import br.com.studyassistant.studyassistantai.repository.DocumentRepository;
import br.com.studyassistant.studyassistantai.repository.TopicRepository;
import br.com.studyassistant.studyassistantai.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import static br.com.studyassistant.studyassistantai.constants.ErrorMessages.*;

@Service
public class DocumentService {

    private final DocumentExtractionService documentExtractionService;
    private final DocumentRepository documentRepository;
    private final ContentRepository contentRepository;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;
    private final ContentVectorService contentVectorService;

    public DocumentService(
            DocumentExtractionService documentExtractionService,
            DocumentRepository documentRepository,
            ContentRepository contentRepository,
            TopicRepository topicRepository,
            UserRepository userRepository,
            ContentVectorService contentVectorService
    ) {
        this.documentExtractionService = documentExtractionService;
        this.documentRepository = documentRepository;
        this.contentRepository = contentRepository;
        this.topicRepository = topicRepository;
        this.userRepository = userRepository;
        this.contentVectorService = contentVectorService;
    }

    @Transactional
    public Content confirmDocument(
            Long userId,
            MultipartFile file,
            Long topicId,
            String title,
            String body
    ) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, USER_ID_REQUIRED);
        }

        if (topicId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, TOPIC_ID_REQUIRED);
        }

        if (title == null || title.isBlank() || body == null || body.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, TITLE_BODY_REQUIRED);
        }

        if (title.trim().length() > 255) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, TITLE_TOO_LONG);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND));

        Topic topic = topicRepository.findByIdAndUserId(topicId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, TOPIC_NOT_FOUND));

        String extractedText = documentExtractionService.extractText(file);

        String filename = file.getOriginalFilename();

        String format = filename.toLowerCase(Locale.ROOT).endsWith(".pdf") ? "PDF" : "TXT";

        Document document = new Document(user, filename, format, file.getSize());

        document.setProcessedAt(LocalDateTime.now());
        document.setExtractedCharacterCount(extractedText.length());

        Document savedDocument = documentRepository.save(document);

        Content content = new Content();

        content.setTitle(title.trim());
        content.setBody(body);
        content.setUser(user);
        content.setTopic(topic);
        content.setOrigin(ContentOrigin.DOCUMENT);
        content.setDocument(savedDocument);

        Content savedContent = contentRepository.save(content);
        contentVectorService.indexContent(savedContent);

        return savedContent;
    }

    @Transactional
    public void deleteDocument(Long documentId, Long userId) {
        Document document = documentRepository.findByIdAndUserId(documentId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, DOCUMENT_NOT_FOUND));

        List<Content> contents = contentRepository.findByDocumentIdAndUserId(documentId, userId);

        for (Content content : contents) {
            contentVectorService.deleteContentVectors(content.getId(), userId);
        }

        contentRepository.deleteAll(contents);
        contentRepository.flush();
        documentRepository.delete(document);
    }

    @Transactional
    public void deleteOrphanDocuments(List<Long> documentIds, Long userId) {
        for (Long documentId : documentIds) {
            Document document = documentRepository.findByIdAndUserId(documentId, userId).orElse(null);

            if (document != null && contentRepository.countByDocumentId(documentId) == 0) {
                documentRepository.delete(document);
            }
        }
    }
}