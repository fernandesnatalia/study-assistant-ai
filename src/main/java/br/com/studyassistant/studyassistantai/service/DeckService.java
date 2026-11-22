package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.dto.DeckAiCreateRequest;
import br.com.studyassistant.studyassistantai.dto.DeckCreateRequest;
import br.com.studyassistant.studyassistantai.dto.GeneratedFlashcard;
import br.com.studyassistant.studyassistantai.model.Deck;
import br.com.studyassistant.studyassistantai.enums.DeckMode;
import br.com.studyassistant.studyassistantai.model.Flashcard;
import br.com.studyassistant.studyassistantai.model.User;
import br.com.studyassistant.studyassistantai.repository.DeckRepository;
import br.com.studyassistant.studyassistantai.repository.FlashcardRepository;
import br.com.studyassistant.studyassistantai.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import static br.com.studyassistant.studyassistantai.constants.ErrorMessages.*;

@Service
public class DeckService {

    private final DeckRepository deckRepository;
    private final FlashcardRepository flashcardRepository;
    private final UserRepository userRepository;
    private final FlashcardAiService flashcardAiService;
    private final PerformanceService performanceService;

    public DeckService(
            DeckRepository deckRepository,
            FlashcardRepository flashcardRepository,
            UserRepository userRepository,
            FlashcardAiService flashcardAiService, PerformanceService performanceService
    ) {
        this.deckRepository = deckRepository;
        this.flashcardRepository = flashcardRepository;
        this.userRepository = userRepository;
        this.flashcardAiService = flashcardAiService;
        this.performanceService = performanceService;
    }

    @Transactional
    public Deck create(DeckCreateRequest request, Long userId) {
        if (request.getMode() != DeckMode.STUDY) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, MANUAL_DECK_STUDY);
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
            throw new ResponseStatusException(HttpStatus.CONFLICT, NO_FLASHCARDS_GENERATED);
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
            throw new ResponseStatusException(HttpStatus.CONFLICT, DECK_CONVERSION_FORBIDDEN);
        }

        performanceService.consolidateDeckPerformance(userId, id);

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

    @Transactional
    public void delete(Long id, Long userId) {
        Deck deck = getExistingDeck(id, userId);

        if (deck.getMode() == DeckMode.TRAINING) {
            performanceService.consolidateDeckPerformance(userId, id);
        }

        deckRepository.delete(deck);
    }

    private User getExistingUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND));
    }

    private Deck getExistingDeck(Long id, Long userId) {
        return deckRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, DECK_NOT_FOUND));
    }
}