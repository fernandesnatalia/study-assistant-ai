package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.dto.DeckPerformanceHistoryResponse;
import br.com.studyassistant.studyassistantai.model.Deck;
import br.com.studyassistant.studyassistantai.model.DeckMode;
import br.com.studyassistant.studyassistantai.model.DeckPerformanceHistory;
import br.com.studyassistant.studyassistantai.model.User;
import br.com.studyassistant.studyassistantai.repository.DeckPerformanceHistoryRepository;
import br.com.studyassistant.studyassistantai.repository.DeckRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeckPerformanceHistoryServiceTest {

    @Mock
    private DeckPerformanceHistoryRepository historyRepository;

    @Mock
    private DeckRepository deckRepository;

    @InjectMocks
    private DeckPerformanceHistoryService service;

    private DeckPerformanceHistory history;
    private Deck deck;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(1L);

        deck = new Deck();
        deck.setId(10L);
        deck.setUser(user);
        deck.setTheme("Algoritmos");
        deck.setMode(DeckMode.STUDY);

        history = new DeckPerformanceHistory(user, 10L, "Algoritmos", 4, 3, 1, 75.0);
    }

    @Test
    void shouldListOnlyUsersPerformanceHistories() {
        when(historyRepository.findByUserId(1L)).thenReturn(List.of(history));

        List<DeckPerformanceHistoryResponse> result = service.findAll(1L);

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).originalDeckId());
        assertEquals("Algoritmos", result.get(0).theme());
        assertEquals(4, result.get(0).totalAttempts());
        assertEquals(3, result.get(0).correctAnswers());
        assertEquals(1, result.get(0).incorrectAnswers());
        assertEquals(75.0, result.get(0).accuracyPercentage());

        verify(historyRepository).findByUserId(1L);
    }

    @Test
    void shouldGetOwnedPerformanceHistory() {
        when(historyRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(history));

        DeckPerformanceHistoryResponse result = service.findById(5L, 1L);

        assertEquals(10L, result.originalDeckId());
        assertEquals("Algoritmos", result.theme());
        assertEquals(75.0, result.accuracyPercentage());
    }

    @Test
    void shouldNotGetAnotherUsersPerformanceHistory() {
        when(historyRepository.findByIdAndUserId(5L, 2L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> service.findById(5L, 2L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void shouldDeleteHistoryAfterDeckConversion() {
        when(historyRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(history));
        when(deckRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(deck));

        service.delete(5L, 1L);

        verify(historyRepository).delete(history);
    }

    @Test
    void shouldDeleteHistoryAfterDeckDeletion() {
        when(historyRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(history));
        when(deckRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.empty());

        service.delete(5L, 1L);

        verify(historyRepository).delete(history);
    }

    @Test
    void shouldNotDeleteHistoryWhileDeckIsInTraining() {
        deck.setMode(DeckMode.TRAINING);

        when(historyRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(history));
        when(deckRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(deck));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> service.delete(5L, 1L)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(historyRepository, never()).delete(history);
    }

    @Test
    void shouldNotDeleteAnotherUsersPerformanceHistory() {
        when(historyRepository.findByIdAndUserId(5L, 2L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> service.delete(5L, 2L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(historyRepository, never()).delete(history);
    }
}
