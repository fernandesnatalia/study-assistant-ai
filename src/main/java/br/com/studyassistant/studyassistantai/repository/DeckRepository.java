package br.com.studyassistant.studyassistantai.repository;

import br.com.studyassistant.studyassistantai.model.Deck;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DeckRepository extends JpaRepository<Deck, Long> {

    List<Deck> findByUserId(Long userId);

    Optional<Deck> findByIdAndUserId(Long id, Long userId);

    void deleteByUserId(Long userId);
}