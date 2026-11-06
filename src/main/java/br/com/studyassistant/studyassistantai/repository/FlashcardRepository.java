package br.com.studyassistant.studyassistantai.repository;

import br.com.studyassistant.studyassistantai.model.Flashcard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FlashcardRepository extends JpaRepository<Flashcard, Long> {

    List<Flashcard> findByContentId(Long contentId);
}