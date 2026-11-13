package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.dto.LoginRequest;
import br.com.studyassistant.studyassistantai.dto.LoginResponse;
import br.com.studyassistant.studyassistantai.model.User;
import br.com.studyassistant.studyassistantai.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldLogin() {
        User user = new User();
        user.setId(1L);
        user.setName("Augusta Ada Byron King");
        user.setEmail("ada@example.com");
        user.setPassword("encoded-password");

        LoginRequest request = new LoginRequest("ada@example.com", "ada123");

        when(userRepository.findByEmail("ada@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("ada123", "encoded-password")).thenReturn(true);
        when(jwtService.generateToken("ada@example.com")).thenReturn("jwt-token");

        LoginResponse response = authService.login(request);

        assertEquals("jwt-token", response.token());
    }

    @Test
    void shouldRejectNonexistentEmail() {
        LoginRequest request = new LoginRequest("ada@example.com", "ada123");

        when(userRepository.findByEmail("ada@example.com")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> authService.login(request)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateToken(anyString());
    }

    @Test
    void shouldRejectInvalidPassword() {
        User user = new User();
        user.setEmail("ada@example.com");
        user.setPassword("encoded-password");

        LoginRequest request = new LoginRequest("ada@example.com", "wrong-password");

        when(userRepository.findByEmail("ada@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> authService.login(request)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        verify(jwtService, never()).generateToken(anyString());
    }
}