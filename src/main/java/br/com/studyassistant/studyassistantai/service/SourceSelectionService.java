package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.model.Content;
import br.com.studyassistant.studyassistantai.model.Topic;
import br.com.studyassistant.studyassistantai.repository.ContentRepository;
import br.com.studyassistant.studyassistantai.repository.SubjectRepository;
import br.com.studyassistant.studyassistantai.repository.TopicRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.Set;
import static br.com.studyassistant.studyassistantai.constants.ErrorMessages.*;

@Service
public class SourceSelectionService {

    private final SubjectRepository subjectRepository;
    private final TopicRepository topicRepository;
    private final ContentRepository contentRepository;

    public SourceSelectionService(
            SubjectRepository subjectRepository,
            TopicRepository topicRepository,
            ContentRepository contentRepository
    ){
        this.subjectRepository = subjectRepository;
        this.topicRepository = topicRepository;
        this.contentRepository = contentRepository;
    }

    public void validate(
            Long userId,
            Set<Long> subjectIds,
            Set<Long> topicIds,
            Set<Long> contentIds
    ){
        if(userId == null) {
            throw new IllegalArgumentException(USER_ID_REQUIRED);
        }

        for(Long subjectId : safe(subjectIds)) {
            if(subjectId == null || subjectRepository.findByIdAndUserId(subjectId, userId).isEmpty()) {
                throw invalidSource();
            }
        }

        for(Long topicId : safe(topicIds)) {
            Topic topic = topicId == null ? null : topicRepository
                    .findByIdAndUserId(topicId, userId).orElse(null);

            if(topic == null) { throw invalidSource(); }

            if(!safe(subjectIds).isEmpty() && !subjectIds.contains(topic.getSubject().getId())) {
                throw invalidSource();
            }
        }

        for(Long contentId : safe(contentIds)) {
            Content content = contentId == null ? null : contentRepository
                    .findByIdAndUserId(contentId, userId).orElse(null);

            if(content == null) { throw invalidSource(); }

            Topic topic = content.getTopic();

            if(!safe(topicIds).isEmpty() && !topicIds.contains(topic.getId())) {
                throw invalidSource();
            }
        }
    }

    private Set<Long> safe(Set<Long> ids) {
        return ids == null ? Set.of() : ids;
    }

    private ResponseStatusException invalidSource() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_SOURCE);
    }
}