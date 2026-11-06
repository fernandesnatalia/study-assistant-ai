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

    public FlashcardService(
            FlashcardRepository flashcardRepository,
            ContentRepository contentRepository
    ) {
        this.flashcardRepository = flashcardRepository;
        this.contentRepository = contentRepository;
    }

    public Flashcard createFlashcard(
            Long contentId,
            Flashcard flashcard
    ) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Content not found"));

        flashcard.setId(null);
        flashcard.setContent(content);

        return flashcardRepository.save(flashcard);
    }

    public List<Flashcard> getAllFlashcards() {
        return flashcardRepository.findAll();
    }

    public Flashcard getFlashcardById(Long id) {
        return flashcardRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Flashcard not found"
                ));
    }

    public List<Flashcard> getFlashcardsByContentId(Long contentId) {
        if (!contentRepository.existsById(contentId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Content not found");
        }

        return flashcardRepository.findByContentId(contentId);
    }

    public Flashcard updateFlashcard(
            Long id,
            Flashcard updatedFlashcard
    ) {
        Flashcard flashcard = getFlashcardById(id);

        flashcard.setQuestion(updatedFlashcard.getQuestion());
        flashcard.setAnswer(updatedFlashcard.getAnswer());

        return flashcardRepository.save(flashcard);
    }

    public void deleteFlashcard(Long id) {
        Flashcard flashcard = getFlashcardById(id);
        flashcardRepository.delete(flashcard);
    }
}