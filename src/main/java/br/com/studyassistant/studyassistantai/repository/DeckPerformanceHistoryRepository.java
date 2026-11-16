package br.com.studyassistant.studyassistantai.repository;

import br.com.studyassistant.studyassistantai.model.DeckPerformanceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DeckPerformanceHistoryRepository extends JpaRepository<DeckPerformanceHistory, Long> {

    List<DeckPerformanceHistory> findByUserId(Long userId);

    Optional<DeckPerformanceHistory> findByIdAndUserId(Long id, Long userId);

    Optional<DeckPerformanceHistory> findByOriginalDeckIdAndUserId(Long originalDeckId, Long userId);

    void deleteByUserId(Long userId);
}
