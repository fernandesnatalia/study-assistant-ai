package br.com.studyassistant.studyassistantai.controller;

import br.com.studyassistant.studyassistantai.model.User;
import br.com.studyassistant.studyassistantai.service.AuthenticatedUserService;
import br.com.studyassistant.studyassistantai.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AuthenticatedUserService authenticatedUserService;

    public UserController(
            UserService userService,
            AuthenticatedUserService authenticatedUserService
    ) {
        this.userService = userService;
        this.authenticatedUserService = authenticatedUserService;
    }

    @PostMapping
    public ResponseEntity<User> createUser(
            @Valid @RequestBody User user
    ) {
        User result = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/me")
    public User getAuthenticatedUser(Authentication authentication) {
        return authenticatedUserService.getAuthenticatedUser(authentication);
    }

    @PutMapping("/me")
    public User updateAuthenticatedUser(
            @Valid @RequestBody User updatedUser,
            Authentication authentication
    ) {
        Long userId = authenticatedUserService.getAuthenticatedUserId(authentication);
        return userService.updateUser(userId, updatedUser);
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAuthenticatedUser(Authentication authentication) {
        Long userId = authenticatedUserService.getAuthenticatedUserId(authentication);
        userService.deleteUser(userId);
    }
}