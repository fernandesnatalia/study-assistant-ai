package br.com.studyassistant.studyassistantai.repository;

import br.com.studyassistant.studyassistantai.model.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ContentRepository extends JpaRepository<Content, Long> {

    List<Content> findByUserId(Long userId);

    Optional<Content> findByIdAndUserId(Long id, Long userId);

    void deleteByUserId(Long userId);
}