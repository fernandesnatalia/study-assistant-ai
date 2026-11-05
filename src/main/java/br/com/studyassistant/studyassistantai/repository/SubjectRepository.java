package br.com.studyassistant.studyassistantai.repository;

import br.com.studyassistant.studyassistantai.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {
}