package br.com.studyassistant.studyassistantai.repository;

import br.com.studyassistant.studyassistantai.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {

    List<Note> findByUserId(Long userId);

    Optional<Note> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndSubjectId(Long userId, Long subjectId);

    boolean existsByUserIdAndTopicId(Long userId, Long topicId);

    boolean existsByUserIdAndContentId(Long userId, Long contentId);

    void deleteByUserId(Long userId);
}