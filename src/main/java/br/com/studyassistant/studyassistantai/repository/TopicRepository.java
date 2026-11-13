package br.com.studyassistant.studyassistantai.repository;

import br.com.studyassistant.studyassistantai.model.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TopicRepository extends JpaRepository<Topic, Long> {

    List<Topic> findByUserId(Long userId);

    Optional<Topic> findByIdAndUserId(Long id, Long userId);

    void deleteByUserId(Long userId);
}