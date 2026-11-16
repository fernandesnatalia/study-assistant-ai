package br.com.studyassistant.studyassistantai.repository;

import br.com.studyassistant.studyassistantai.model.AnswerAttempt;
import br.com.studyassistant.studyassistantai.model.DeckMode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AnswerAttemptRepository extends JpaRepository<AnswerAttempt, Long> {

    List<AnswerAttempt> findByUserIdOrderByAttemptedAtDescIdDesc(Long userId);

    List<AnswerAttempt> findByUserIdAndFlashcardIdOrderByAttemptedAtDescIdDesc(Long userId, Long flashcardId);

    List<AnswerAttempt> findByUserIdAndFlashcardDeckId(Long userId, Long deckId);

    List<AnswerAttempt> findByUserIdAndFlashcardDeckModeOrderByAttemptedAtDescIdDesc(Long userId, DeckMode mode);

    void deleteByFlashcardDeckId(Long deckId);

    void deleteByUserId(Long userId);
}