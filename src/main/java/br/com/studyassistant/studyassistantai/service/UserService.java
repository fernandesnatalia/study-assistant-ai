package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.model.User;
import br.com.studyassistant.studyassistantai.repository.AnswerAttemptRepository;
import br.com.studyassistant.studyassistantai.repository.ContentRepository;
import br.com.studyassistant.studyassistantai.repository.DeckRepository;
import br.com.studyassistant.studyassistantai.repository.FlashcardRepository;
import br.com.studyassistant.studyassistantai.repository.NoteRepository;
import br.com.studyassistant.studyassistantai.repository.SubjectRepository;
import br.com.studyassistant.studyassistantai.repository.TopicRepository;
import br.com.studyassistant.studyassistantai.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final AnswerAttemptRepository answerAttemptRepository;
    private final NoteRepository noteRepository;
    private final FlashcardRepository flashcardRepository;
    private final DeckRepository deckRepository;
    private final ContentRepository contentRepository;
    private final TopicRepository topicRepository;
    private final SubjectRepository subjectRepository;

    public UserService(
            UserRepository userRepository,
            AnswerAttemptRepository answerAttemptRepository,
            NoteRepository noteRepository,
            FlashcardRepository flashcardRepository,
            DeckRepository deckRepository,
            ContentRepository contentRepository,
            TopicRepository topicRepository,
            SubjectRepository subjectRepository
    ) {
        this.userRepository = userRepository;
        this.answerAttemptRepository = answerAttemptRepository;
        this.noteRepository = noteRepository;
        this.flashcardRepository = flashcardRepository;
        this.deckRepository = deckRepository;
        this.contentRepository = contentRepository;
        this.topicRepository = topicRepository;
        this.subjectRepository = subjectRepository;
    }

    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public User updateUser(Long id, User updatedUser) {
        User user = getUserById(id);

        if (!user.getEmail().equals(updatedUser.getEmail())
                && userRepository.existsByEmail(updatedUser.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        user.setName(updatedUser.getName());
        user.setEmail(updatedUser.getEmail());

        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);

        answerAttemptRepository.deleteByUserId(id);
        noteRepository.deleteByUserId(id);

        flashcardRepository.deleteByDeckUserId(id);
        deckRepository.deleteByUserId(id);

        contentRepository.deleteByUserId(id);
        topicRepository.deleteByUserId(id);
        subjectRepository.deleteByUserId(id);

        userRepository.delete(user);
    }
}