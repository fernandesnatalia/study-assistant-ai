package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.dto.GeneratedFlashcard;
import br.com.studyassistant.studyassistantai.model.Content;
import br.com.studyassistant.studyassistantai.model.Deck;
import br.com.studyassistant.studyassistantai.model.Flashcard;
import br.com.studyassistant.studyassistantai.model.Topic;
import br.com.studyassistant.studyassistantai.repository.ContentRepository;
import br.com.studyassistant.studyassistantai.repository.DeckRepository;
import br.com.studyassistant.studyassistantai.repository.FlashcardRepository;
import br.com.studyassistant.studyassistantai.repository.TopicRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class FlashcardService {

    private final FlashcardRepository flashcardRepository;
    private final DeckRepository deckRepository;
    private final ContentRepository contentRepository;
    private final TopicRepository topicRepository;
    private final FlashcardAiService flashcardAiService;

    public FlashcardService(
            FlashcardRepository flashcardRepository,
            DeckRepository deckRepository,
            ContentRepository contentRepository,
            TopicRepository topicRepository,
            FlashcardAiService flashcardAiService
    ) {
        this.flashcardRepository = flashcardRepository;
        this.deckRepository = deckRepository;
        this.contentRepository = contentRepository;
        this.topicRepository = topicRepository;
        this.flashcardAiService = flashcardAiService;
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

    @Transactional
    public List<Flashcard> generateFlashcards(
            Long userId,
            Long deckId,
            int quantity,
            Long contentId,
            Long topicId,
            String context
    ) {
        Deck deck = deckRepository.findByIdAndUserId(deckId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Deck not found"));

        String generationContext = buildGenerationContext(
                userId,
                contentId,
                topicId,
                context
        );

        List<GeneratedFlashcard> generatedFlashcards = flashcardAiService.generateFlashcards(
                deck.getTheme(),
                quantity,
                generationContext
        );

        List<Flashcard> flashcards = generatedFlashcards.stream()
                .map(generatedFlashcard -> {
                    Flashcard flashcard = new Flashcard();
                    flashcard.setQuestion(generatedFlashcard.question());
                    flashcard.setAnswer(generatedFlashcard.answer());
                    flashcard.setDeck(deck);
                    return flashcard;
                })
                .toList();

        return flashcardRepository.saveAll(flashcards);
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

        String generatedAnswer = flashcardAiService.generateAnswer(
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

    private String buildGenerationContext(
            Long userId,
            Long contentId,
            Long topicId,
            String context
    ) {
        StringBuilder generationContext = new StringBuilder();

        if (contentId != null) {
            Content content = contentRepository.findByIdAndUserId(contentId, userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Content not found"));

            generationContext
                    .append("Conteúdo de estudo selecionado:\n")
                    .append("Título: ")
                    .append(content.getTitle())
                    .append("\n")
                    .append("Conteúdo: ")
                    .append(content.getBody())
                    .append("\n");
        }

        if (topicId != null) {
            Topic topic = topicRepository.findByIdAndUserId(topicId, userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Topic not found"));

            if (!generationContext.isEmpty()) {
                generationContext.append("\n");
            }

            generationContext
                    .append("Tópico selecionado:\n")
                    .append(topic.getName())
                    .append("\n");
        }

        if (context != null && !context.isBlank()) {
            if (!generationContext.isEmpty()) {
                generationContext.append("\n");
            }

            generationContext
                    .append("Contexto adicional fornecido pelo usuário:\n")
                    .append(context.trim())
                    .append("\n");
        }

        return generationContext.toString().trim();
    }

    private void validateDeckIsNotInTraining(Deck deck) {
        if (deck.isTraining()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Flashcards cannot be manually modified while deck is in training"
            );
        }
    }
}