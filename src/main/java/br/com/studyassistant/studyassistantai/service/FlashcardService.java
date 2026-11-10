package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.model.Content;
import br.com.studyassistant.studyassistantai.model.Flashcard;
import br.com.studyassistant.studyassistantai.repository.ContentRepository;
import br.com.studyassistant.studyassistantai.repository.FlashcardRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class FlashcardService {

    private final FlashcardRepository flashcardRepository;
    private final ContentRepository contentRepository;
    private final GenerativeService generativeService;

    public FlashcardService(
            FlashcardRepository flashcardRepository,
            ContentRepository contentRepository, GenerativeService generativeService
    ) {
        this.flashcardRepository = flashcardRepository;
        this.contentRepository = contentRepository;
        this.generativeService = generativeService;
    }

    public Flashcard createFlashcard(
            Long userId,
            Long contentId,
            Flashcard flashcard
    ) {
        Content content = contentRepository.findByIdAndUserId(contentId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Content not found"));

        flashcard.setId(null);
        flashcard.setContent(content);

        return flashcardRepository.save(flashcard);
    }

    public List<Flashcard> getAllFlashcards(Long userId) {
        return flashcardRepository.findByContentUserId(userId);
    }

    public Flashcard getFlashcardById(Long id, Long userId) {
        return flashcardRepository.findByIdAndContentUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Flashcard not found"
                ));
    }

    public List<Flashcard> getFlashcardsByContentId(Long contentId, Long userId) {
        if (contentRepository.findByIdAndUserId(contentId, userId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Content not found");
        }

        return flashcardRepository.findByContentIdAndContentUserId(contentId, userId);
    }

    public Flashcard updateFlashcard(
            Long id,
            Long userId,
            Flashcard updatedFlashcard
    ) {
        Flashcard flashcard = getFlashcardById(id, userId);

        flashcard.setQuestion(updatedFlashcard.getQuestion());
        flashcard.setAnswer(updatedFlashcard.getAnswer());

        return flashcardRepository.save(flashcard);
    }

    public Flashcard generateFlashcardAnswer(Long id, Long userId) {
        Flashcard flashcard = getFlashcardById(id, userId);

        if (flashcard.getAnswer() != null && !flashcard.getAnswer().isBlank()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Flashcard already has an answer");
        }

        Content content = flashcard.getContent();
        String generatedAnswer = generativeService.generateAnswer(flashcard.getQuestion(), content.getBody());
        flashcard.setAnswer(generatedAnswer);
        return flashcardRepository.save(flashcard);
    }

    public void deleteFlashcard(Long id, Long userId) {
        Flashcard flashcard = getFlashcardById(id, userId);
        flashcardRepository.delete(flashcard);
    }
}