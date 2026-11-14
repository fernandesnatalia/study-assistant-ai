package br.com.studyassistant.studyassistantai.repository;

import br.com.studyassistant.studyassistantai.model.AnswerAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AnswerAttemptRepository extends JpaRepository<AnswerAttempt, Long> {

    List<AnswerAttempt> findByUserIdOrderByAttemptedAtDescIdDesc(Long userId);

    List<AnswerAttempt> findByUserIdAndFlashcardIdOrderByAttemptedAtDescIdDesc(Long userId, Long flashcardId);

    List<AnswerAttempt> findByUserIdAndFlashcardDeckId(Long userId, Long deckId);

    List<AnswerAttempt> findByUserIdAndFlashcardDeckTrainingTrueOrderByAttemptedAtDescIdDesc(Long userId);

    void deleteByFlashcardDeckId(Long deckId);

    void deleteByUserId(Long userId);
}