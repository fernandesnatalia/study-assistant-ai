package br.com.studyassistant.studyassistantai.repository;

import br.com.studyassistant.studyassistantai.model.AnswerAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AnswerAttemptRepository extends JpaRepository<AnswerAttempt, Long> {

    List<AnswerAttempt> findByUserIdOrderByAnsweredAtDescIdDesc(Long userId);

    List<AnswerAttempt> findByUserIdAndFlashcardIdOrderByAnsweredAtDescIdDesc(
            Long userId,
            Long flashcardId
    );
}