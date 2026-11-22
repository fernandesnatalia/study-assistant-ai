package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.model.User;
import br.com.studyassistant.studyassistantai.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticatedUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthenticatedUserService authenticatedUserService;

    private static final String EMAIL = "ada@example.com";

    @Test
    void shouldGetAuthenticatedUser() {
        User user = new User();
        user.setId(1L);
        user.setName("Ada Lovelace");
        user.setEmail(EMAIL);

        when(authentication.getName()).thenReturn(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        User result = authenticatedUserService.getAuthenticatedUser(authentication);

        assertSame(user, result);
    }

    @Test
    void shouldGetAuthenticatedUserId() {
        User user = new User();
        user.setId(1L);
        user.setEmail(EMAIL);

        when(authentication.getName()).thenReturn(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        Long result = authenticatedUserService.getAuthenticatedUserId(authentication);

        assertEquals(1L, result);
    }

    @Test
    void shouldRejectAuthenticatedUserNotFound() {
        when(authentication.getName()).thenReturn(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> authenticatedUserService.getAuthenticatedUser(authentication)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }
}