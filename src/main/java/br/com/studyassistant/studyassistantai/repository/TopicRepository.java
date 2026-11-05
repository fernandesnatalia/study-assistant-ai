package br.com.studyassistant.studyassistantai.repository;

import br.com.studyassistant.studyassistantai.model.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopicRepository extends JpaRepository<Topic, Long> {
}