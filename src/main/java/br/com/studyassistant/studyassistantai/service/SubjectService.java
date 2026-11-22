package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.model.Subject;
import br.com.studyassistant.studyassistantai.model.Topic;
import br.com.studyassistant.studyassistantai.model.User;
import br.com.studyassistant.studyassistantai.repository.SubjectRepository;
import br.com.studyassistant.studyassistantai.repository.UserRepository;
import br.com.studyassistant.studyassistantai.repository.DocumentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static br.com.studyassistant.studyassistantai.constants.ErrorMessages.*;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private final DocumentService documentService;

    public SubjectService(
            SubjectRepository subjectRepository,
            UserRepository userRepository,
            DocumentRepository documentRepository,
            DocumentService documentService
    ) {
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
        this.documentRepository = documentRepository;
        this.documentService = documentService;
    }

    public Subject createSubject(Subject subject, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND));

        subject.setUser(user);

        return subjectRepository.save(subject);
    }

    public List<Subject> getAllSubjects(Long userId) {
        return subjectRepository.findByUserId(userId);
    }

    public Subject getSubjectById(Long id, Long userId) {
        return subjectRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, SUBJECT_NOT_FOUND));
    }

    public Subject updateSubject(Long id, Long userId, Subject updatedSubject) {
        Subject subject = getSubjectById(id, userId);
        subject.setName(updatedSubject.getName());
        return subjectRepository.save(subject);
    }

    @Transactional
    public void deleteSubject(Long id, Long userId) {
        Subject subject = getSubjectById(id, userId);

        Set<Long> documentIds = new HashSet<>();

        for (Topic topic : subject.getTopics()) {
            documentIds.addAll(documentRepository.findDocumentIdsByTopicIdAndUserId(topic.getId(), userId));
        }

        subjectRepository.delete(subject);
        subjectRepository.flush();

        documentService.deleteOrphanDocuments(new ArrayList<>(documentIds), userId);
    }

    @Transactional(readOnly = true)
    public Set<Topic> getSubjectTopics(Long subjectId, Long userId) {
        Subject subject = getSubjectById(subjectId, userId);
        return Set.copyOf(subject.getTopics());
    }
}