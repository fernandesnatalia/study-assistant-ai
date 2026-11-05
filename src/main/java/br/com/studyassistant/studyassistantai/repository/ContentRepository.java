package br.com.studyassistant.studyassistantai.repository;

import br.com.studyassistant.studyassistantai.model.Content;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentRepository extends JpaRepository<Content, Long> {
}