package br.com.studyassistant.studyassistantai.repository;

import br.com.studyassistant.studyassistantai.model.Flashcard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlashcardRepository extends JpaRepository<Flashcard, Long> {

    List<Flashcard> findByDeckUserId(Long userId);

    Optional<Flashcard> findByIdAndDeckUserId(Long id, Long userId);

    List<Flashcard> findByDeckIdAndDeckUserId(Long deckId, Long userId);

    long countByDeckId(Long deckId);

    void deleteByDeckUserId(Long userId);
}