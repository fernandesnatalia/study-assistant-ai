package br.com.studyassistant.studyassistantai.repository;

import br.com.studyassistant.studyassistantai.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);
}
