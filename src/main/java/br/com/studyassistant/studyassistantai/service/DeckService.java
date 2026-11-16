package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.dto.DeckAiCreateRequest;
import br.com.studyassistant.studyassistantai.dto.DeckCreateRequest;
import br.com.studyassistant.studyassistantai.dto.GeneratedFlashcard;
import br.com.studyassistant.studyassistantai.model.Deck;
import br.com.studyassistant.studyassistantai.model.DeckMode;
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
    private final FlashcardAiService flashcardAiService;

    public DeckService(
            DeckRepository deckRepository,
            FlashcardRepository flashcardRepository,
            UserRepository userRepository,
            AnswerAttemptRepository answerAttemptRepository,
            FlashcardAiService flashcardAiService
    ) {
        this.deckRepository = deckRepository;
        this.flashcardRepository = flashcardRepository;
        this.userRepository = userRepository;
        this.answerAttemptRepository = answerAttemptRepository;
        this.flashcardAiService = flashcardAiService;
    }

    @Transactional
    public Deck create(DeckCreateRequest request, Long userId) {
        if (request.getMode() != DeckMode.STUDY) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Manually created decks must use STUDY mode");
        }

        User user = getExistingUser(userId);

        Deck deck = new Deck();
        deck.setTheme(request.getTheme());
        deck.setUser(user);
        deck.setMode(request.getMode());

        Deck savedDeck = deckRepository.save(deck);

        Flashcard flashcard = new Flashcard();
        flashcard.setQuestion(request.getQuestion());
        flashcard.setAnswer(request.getAnswer());
        flashcard.setDeck(savedDeck);

        flashcardRepository.save(flashcard);

        return savedDeck;
    }

    @Transactional
    public Deck createWithAi(DeckAiCreateRequest request, Long userId) {
        User user = getExistingUser(userId);

        List<GeneratedFlashcard> generatedFlashcards = flashcardAiService.generateFlashcards(
                request.theme(),
                request.quantity(),
                ""
        );

        if (generatedFlashcards.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "AI did not generate any flashcards");
        }

        Deck deck = new Deck();
        deck.setTheme(request.theme());
        deck.setUser(user);
        deck.setMode(request.mode());

        Deck savedDeck = deckRepository.save(deck);

        List<Flashcard> flashcards = generatedFlashcards.stream()
                .map(generatedFlashcard -> {
                    Flashcard flashcard = new Flashcard();
                    flashcard.setQuestion(generatedFlashcard.question());
                    flashcard.setAnswer(generatedFlashcard.answer());
                    flashcard.setDeck(savedDeck);
                    return flashcard;
                })
                .toList();

        flashcardRepository.saveAll(flashcards);

        return savedDeck;
    }

    @Transactional
    public Deck convertToStudy(Long id, Long userId) {
        Deck deck = getExistingDeck(id, userId);

        if (deck.getMode() != DeckMode.TRAINING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only TRAINING decks can be converted to STUDY");
        }

        if (!answerAttemptRepository
                .findByUserIdAndFlashcardDeckId(userId, id)
                .isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Deck performance history must be preserved before conversion"
            );
        }

        deck.setMode(DeckMode.STUDY);

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

        if (deck.getMode() == DeckMode.TRAINING && !answerAttemptRepository
                .findByUserIdAndFlashcardDeckId(userId, id)
                .isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Deck performance history must be preserved before deletion"
            );
        }

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