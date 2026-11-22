package br.com.studyassistant.studyassistantai.repository;

import br.com.studyassistant.studyassistantai.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    @Query("""
    SELECT DISTINCT d.id
    FROM Document d
    JOIN Content c ON c.document = d
    WHERE c.topic.id = :topicId
    AND d.user.id = :userId
    """)
    List<Long> findDocumentIdsByTopicIdAndUserId(Long topicId, Long userId);

    List<Document> findByUserId(Long userId);

    Optional<Document> findByIdAndUserId(Long id, Long userId);

    void deleteByUserId(Long userId);
}