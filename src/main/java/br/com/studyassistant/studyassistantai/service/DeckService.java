package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.dto.DeckCreateRequest;
import br.com.studyassistant.studyassistantai.model.Deck;
import br.com.studyassistant.studyassistantai.model.Flashcard;
import br.com.studyassistant.studyassistantai.model.User;
import br.com.studyassistant.studyassistantai.repository.AnswerAttemptRepository;
import br.com.studyassistant.studyassistantai.repository.DeckRepository;
import br.com.studyassistant.studyassistantai.repository.FlashcardRepository;
import br.com.studyassistant.studyassistantai.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class DeckService {

    private final DeckRepository deckRepository;
    private final FlashcardRepository flashcardRepository;
    private final UserRepository userRepository;
    private final AnswerAttemptRepository answerAttemptRepository;

    public DeckService(
            DeckRepository deckRepository,
            FlashcardRepository flashcardRepository,
            UserRepository userRepository,
            AnswerAttemptRepository answerAttemptRepository

    ) {
        this.deckRepository = deckRepository;
        this.flashcardRepository = flashcardRepository;
        this.userRepository = userRepository;
        this.answerAttemptRepository = answerAttemptRepository;
    }

    @Transactional
    public Deck create(DeckCreateRequest request, Long userId) {
        User user = getExistingUser(userId);

        Deck deck = new Deck();
        deck.setTheme(request.getTheme());
        deck.setUser(user);
        deck.setTraining(false);

        Deck savedDeck = deckRepository.save(deck);

        Flashcard flashcard = new Flashcard();
        flashcard.setQuestion(request.getQuestion());
        flashcard.setAnswer(request.getAnswer());
        flashcard.setDeck(savedDeck);

        flashcardRepository.save(flashcard);

        return savedDeck;
    }

    @Transactional
    public Deck startTraining(Long id, Long userId) {
        Deck deck = findById(id, userId);

        deck.setTraining(true);

        return deckRepository.save(deck);
    }

    @Transactional
    public Deck stopTraining(Long id, Long userId) {
        Deck deck = findById(id, userId);

        answerAttemptRepository.deleteByFlashcardDeckId(deck.getId());

        deck.setTraining(false);

        return deckRepository.save(deck);
    }

    public List<Deck> findAll(Long userId) {
        getExistingUser(userId);

        return deckRepository.findByUserId(userId);
    }

    public Deck findById(Long id, Long userId) {
        return getExistingDeck(id, userId);
    }

    public Deck update(Long id, Deck updatedDeck, Long userId) {
        Deck existingDeck = getExistingDeck(id, userId);

        existingDeck.setTheme(updatedDeck.getTheme());

        return deckRepository.save(existingDeck);
    }

    public void delete(Long id, Long userId) {
        Deck deck = getExistingDeck(id, userId);

        deckRepository.delete(deck);
    }

    private User getExistingUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private Deck getExistingDeck(Long id, Long userId) {
        return deckRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Deck not found"));
    }
}