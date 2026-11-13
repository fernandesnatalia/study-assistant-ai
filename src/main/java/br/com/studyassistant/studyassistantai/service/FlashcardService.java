package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.model.Deck;
import br.com.studyassistant.studyassistantai.model.Flashcard;
import br.com.studyassistant.studyassistantai.repository.DeckRepository;
import br.com.studyassistant.studyassistantai.repository.FlashcardRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class FlashcardService {

    private final FlashcardRepository flashcardRepository;
    private final DeckRepository deckRepository;
    private final GenerativeService generativeService;

    public FlashcardService(
            FlashcardRepository flashcardRepository,
            DeckRepository deckRepository,
            GenerativeService generativeService
    ) {
        this.flashcardRepository = flashcardRepository;
        this.deckRepository = deckRepository;
        this.generativeService = generativeService;
    }

    public Flashcard createFlashcard(
            Long userId,
            Long deckId,
            Flashcard flashcard
    ) {
        Deck deck = deckRepository.findByIdAndUserId(deckId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Deck not found"));

        validateDeckIsNotInTraining(deck);

        flashcard.setId(null);
        flashcard.setDeck(deck);

        return flashcardRepository.save(flashcard);
    }

    public List<Flashcard> getAllFlashcards(Long userId) {
        return flashcardRepository.findByDeckUserId(userId);
    }

    public Flashcard getFlashcardById(Long id, Long userId) {
        return flashcardRepository.findByIdAndDeckUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Flashcard not found"));
    }

    public List<Flashcard> getFlashcardsByDeckId(Long deckId, Long userId) {
        if (deckRepository.findByIdAndUserId(deckId, userId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Deck not found");
        }

        return flashcardRepository.findByDeckIdAndDeckUserId(deckId, userId);
    }

    public Flashcard updateFlashcard(
            Long id,
            Long userId,
            Flashcard updatedFlashcard
    ) {
        Flashcard flashcard = getFlashcardById(id, userId);

        validateDeckIsNotInTraining(flashcard.getDeck());

        flashcard.setQuestion(updatedFlashcard.getQuestion());
        flashcard.setAnswer(updatedFlashcard.getAnswer());

        return flashcardRepository.save(flashcard);
    }

    public Flashcard generateFlashcardAnswer(Long id, Long userId) {
        Flashcard flashcard = getFlashcardById(id, userId);

        validateDeckIsNotInTraining(flashcard.getDeck());

        if (flashcard.getAnswer() != null && !flashcard.getAnswer().isBlank()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Flashcard already has an answer");
        }

        String generatedAnswer = generativeService.generateAnswer(
                flashcard.getQuestion(),
                flashcard.getDeck().getTheme()
        );

        flashcard.setAnswer(generatedAnswer);

        return flashcardRepository.save(flashcard);
    }

    public void deleteFlashcard(Long id, Long userId) {
        Flashcard flashcard = getFlashcardById(id, userId);
        Deck deck = flashcard.getDeck();
        validateDeckIsNotInTraining(flashcard.getDeck());

        flashcardRepository.delete(flashcard);

        if (flashcardRepository.countByDeckId(deck.getId()) == 0) {
            deckRepository.delete(deck);
        }
    }

    private void validateDeckIsNotInTraining(Deck deck) {
        if (deck.isTraining()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Flashcards cannot be manually modified while deck is in training"
            );
        }
    }
}