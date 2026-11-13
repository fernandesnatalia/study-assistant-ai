package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.model.Content;
import br.com.studyassistant.studyassistantai.model.Note;
import br.com.studyassistant.studyassistantai.model.Subject;
import br.com.studyassistant.studyassistantai.model.Topic;
import br.com.studyassistant.studyassistantai.model.User;
import br.com.studyassistant.studyassistantai.repository.ContentRepository;
import br.com.studyassistant.studyassistantai.repository.NoteRepository;
import br.com.studyassistant.studyassistantai.repository.SubjectRepository;
import br.com.studyassistant.studyassistantai.repository.TopicRepository;
import br.com.studyassistant.studyassistantai.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final TopicRepository topicRepository;
    private final ContentRepository contentRepository;

    public NoteService(
            NoteRepository noteRepository,
            UserRepository userRepository,
            SubjectRepository subjectRepository,
            TopicRepository topicRepository,
            ContentRepository contentRepository
    ) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
        this.topicRepository = topicRepository;
        this.contentRepository = contentRepository;
    }

    public Note create(Note note, Long userId) {
        User user = getExistingUser(userId);

        validateContext(note);

        if (note.getSubject() != null) {
            Subject subject = getExistingSubject(note.getSubject().getId(), userId);

            if (noteRepository.existsByUserIdAndSubjectId(userId, subject.getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Note already exists for this subject");
            }

            note.setSubject(subject);
        }

        if (note.getTopic() != null) {
            Topic topic = getExistingTopic(note.getTopic().getId(), userId);

            if (noteRepository.existsByUserIdAndTopicId(userId, topic.getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Note already exists for this topic");
            }

            note.setTopic(topic);
        }

        if (note.getContent() != null) {
            Content content = getExistingContent(note.getContent().getId(), userId);

            if (noteRepository.existsByUserIdAndContentId(userId, content.getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Note already exists for this content");
            }

            note.setContent(content);
        }

        note.setId(null);
        note.setUser(user);

        return noteRepository.save(note);
    }

    public List<Note> findAll(Long userId) {
        getExistingUser(userId);

        return noteRepository.findByUserId(userId);
    }

    public Note findById(Long id, Long userId) {
        return getExistingNote(id, userId);
    }

    public Note update(Long id, Note updatedNote, Long userId) {
        Note existingNote = getExistingNote(id, userId);

        existingNote.setTitle(updatedNote.getTitle());
        existingNote.setBody(updatedNote.getBody());

        return noteRepository.save(existingNote);
    }

    public void delete(Long id, Long userId) {
        Note note = getExistingNote(id, userId);

        noteRepository.delete(note);
    }

    private void validateContext(Note note) {
        int contexts = 0;

        if (note.getSubject() != null) {
            contexts++;
        }

        if (note.getTopic() != null) {
            contexts++;
        }

        if (note.getContent() != null) {
            contexts++;
        }

        if (contexts != 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Note must have exactly one context");
        }

        if (note.getSubject() != null && note.getSubject().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subject ID is required");
        }

        if (note.getTopic() != null && note.getTopic().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Topic ID is required");
        }

        if (note.getContent() != null && note.getContent().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Content ID is required");
        }
    }

    private User getExistingUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private Subject getExistingSubject(Long subjectId, Long userId) {
        return subjectRepository.findByIdAndUserId(subjectId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subject not found"));
    }

    private Topic getExistingTopic(Long topicId, Long userId) {
        return topicRepository.findByIdAndUserId(topicId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Topic not found"));
    }

    private Content getExistingContent(Long contentId, Long userId) {
        return contentRepository.findByIdAndUserId(contentId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Content not found"));
    }

    private Note getExistingNote(Long id, Long userId) {
        return noteRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found"));
    }
}