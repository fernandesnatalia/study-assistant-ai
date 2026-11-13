package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.model.User;
import br.com.studyassistant.studyassistantai.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthenticatedUserService {

    private final UserRepository userRepository;

    public AuthenticatedUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getAuthenticatedUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
    }

    public Long getAuthenticatedUserId(Authentication authentication) {
        return getAuthenticatedUser(authentication).getId();
    }
}