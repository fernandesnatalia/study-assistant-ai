package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.dto.DeckCreateRequest;
import br.com.studyassistant.studyassistantai.model.Deck;
import br.com.studyassistant.studyassistantai.model.Flashcard;
import br.com.studyassistant.studyassistantai.model.User;
import br.com.studyassistant.studyassistantai.repository.DeckRepository;
import br.com.studyassistant.studyassistantai.repository.FlashcardRepository;
import br.com.studyassistant.studyassistantai.repository.UserRepository;
import br.com.studyassistant.studyassistantai.repository.AnswerAttemptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeckServiceTest {

    @Mock
    private DeckRepository deckRepository;

    @Mock
    private FlashcardRepository flashcardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AnswerAttemptRepository answerAttemptRepository;

    @InjectMocks
    private DeckService deckService;

    private User user;
    private Deck deck;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        deck = new Deck();
        deck.setId(1L);
        deck.setTheme("Algoritmos");
        deck.setUser(user);
    }

    @Test
    void shouldCreateDeckWithFirstFlashcard() {
        DeckCreateRequest request = new DeckCreateRequest();
        request.setTheme("Algoritmos");
        request.setQuestion("O que é um algoritmo?");
        request.setAnswer("É uma sequência finita de instruções para resolver um problema.");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        when(deckRepository.save(any(Deck.class)))
                .thenAnswer(invocation -> {
                    Deck savedDeck = invocation.getArgument(0);
                    savedDeck.setId(1L);
                    return savedDeck;
        });

        when(flashcardRepository.save(any(Flashcard.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Deck result = deckService.create(request, 1L);

        assertEquals(1L, result.getId());
        assertEquals("Algoritmos", result.getTheme());
        assertSame(user, result.getUser());

        ArgumentCaptor<Flashcard> flashcardCaptor = ArgumentCaptor.forClass(Flashcard.class);

        verify(flashcardRepository).save(flashcardCaptor.capture());

        Flashcard savedFlashcard = flashcardCaptor.getValue();

        assertEquals("O que é um algoritmo?", savedFlashcard.getQuestion());
        assertEquals(
                "É uma sequência finita de instruções para resolver um problema.",
                savedFlashcard.getAnswer()
        );

        assertSame(result, savedFlashcard.getDeck());

        verify(deckRepository).save(any(Deck.class));
    }

    @Test
    void shouldNotCreateDeckForNonexistentUser() {
        DeckCreateRequest request = new DeckCreateRequest();
        request.setTheme("Algoritmos");
        request.setQuestion("O que é um algoritmo?");
        request.setAnswer("É uma sequência finita de instruções para resolver um problema.");

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> deckService.create(request, 99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(deckRepository, never()).save(any(Deck.class));
        verify(flashcardRepository, never()).save(any(Flashcard.class));
    }

    @Test
    void shouldGetOnlyDecksFromUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(deckRepository.findByUserId(1L)).thenReturn(List.of(deck));

        List<Deck> result = deckService.findAll(1L);

        assertEquals(1, result.size());
        assertSame(deck, result.get(0));
    }

    @Test
    void shouldNotGetDecksForNonexistentUser() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> deckService.findAll(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(deckRepository, never()).findByUserId(99L);
    }

    @Test
    void shouldGetOwnedDeckById() {
        when(deckRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(deck));

        Deck result = deckService.findById(1L, 1L);

        assertSame(deck, result);
    }

    @Test
    void shouldNotGetAnotherUsersDeck() {
        when(deckRepository.findByIdAndUserId(1L, 2L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> deckService.findById(1L, 2L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void shouldUpdateOwnedDeckTheme() {
        Deck updatedDeck = new Deck();
        updatedDeck.setTheme("Sequências Numéricas");

        when(deckRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(deck));
        when(deckRepository.save(deck)).thenReturn(deck);

        Deck result = deckService.update(1L, updatedDeck, 1L);

        assertEquals("Sequências Numéricas", result.getTheme());

        verify(deckRepository).save(deck);
    }

    @Test
    void shouldNotUpdateAnotherUsersDeck() {
        Deck updatedDeck = new Deck();
        updatedDeck.setTheme("Sequências Numéricas");

        when(deckRepository.findByIdAndUserId(1L, 2L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> deckService.update(1L, updatedDeck, 2L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(deckRepository, never()).save(any(Deck.class));
    }

    @Test
    void shouldDeleteOwnedDeck() {
        when(deckRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(deck));

        deckService.delete(1L, 1L);

        verify(deckRepository).delete(deck);
    }

    @Test
    void shouldNotDeleteAnotherUsersDeck() {
        when(deckRepository.findByIdAndUserId(1L, 2L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> deckService.delete(1L, 2L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(deckRepository, never()).delete(any(Deck.class));
    }

    @Test
    void shouldStartTraining() {
        when(deckRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(deck));
        when(deckRepository.save(deck)).thenReturn(deck);

        Deck result = deckService.startTraining(1L, 1L);

        assertTrue(result.isTraining());

        verify(deckRepository).save(deck);
    }

    @Test
    void shouldStopTrainingAndDeletePreviousAttempts() {
        deck.setTraining(true);

        when(deckRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(deck));
        when(deckRepository.save(deck)).thenReturn(deck);

        Deck result = deckService.stopTraining(1L, 1L);

        assertFalse(result.isTraining());

        verify(answerAttemptRepository).deleteByFlashcardDeckId(1L);
        verify(deckRepository).save(deck);
    }

    @Test
    void shouldNotStartTrainingForAnotherUsersDeck() {
        when(deckRepository.findByIdAndUserId(1L, 2L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> deckService.startTraining(1L, 2L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(deckRepository, never()).save(any(Deck.class));
    }

    @Test
    void shouldNotStopTrainingForAnotherUsersDeck() {
        when(deckRepository.findByIdAndUserId(1L, 2L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> deckService.stopTraining(1L, 2L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(answerAttemptRepository, never()).deleteByFlashcardDeckId(any());
        verify(deckRepository, never()).save(any(Deck.class));
    }
}