package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.model.Deck;
import br.com.studyassistant.studyassistantai.model.Flashcard;
import br.com.studyassistant.studyassistantai.model.User;
import br.com.studyassistant.studyassistantai.repository.DeckRepository;
import br.com.studyassistant.studyassistantai.repository.FlashcardRepository;
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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlashcardServiceTest {

    @Mock
    private FlashcardRepository flashcardRepository;

    @Mock
    private DeckRepository deckRepository;

    @Mock
    private FlashcardAiService flashcardAiService;

    @InjectMocks
    private FlashcardService flashcardService;

    private Flashcard flashcard;
    private Deck deck;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(1L);

        deck = new Deck();
        deck.setId(1L);
        deck.setTheme("Algoritmos");
        deck.setUser(user);

        flashcard = new Flashcard();
        flashcard.setId(1L);
        flashcard.setQuestion("O que é um algoritmo?");
        flashcard.setDeck(deck);
    }

    @Test
    void shouldGenerateAnswerForFlashcardWithoutAnswer() {
        String generatedAnswer = "É uma sequência finita de instruções para resolver um problema.";

        when(flashcardRepository.findByIdAndDeckUserId(1L, 1L)).thenReturn(Optional.of(flashcard));
        when(flashcardAiService.generateAnswer(flashcard.getQuestion(), deck.getTheme())).thenReturn(generatedAnswer);
        when(flashcardRepository.save(flashcard)).thenReturn(flashcard);

        Flashcard result = flashcardService.generateFlashcardAnswer(1L, 1L);

        assertEquals(generatedAnswer, result.getAnswer());

        verify(flashcardAiService).generateAnswer(flashcard.getQuestion(), deck.getTheme());
        verify(flashcardRepository).save(flashcard);
    }

    @Test
    void shouldNotGenerateAnswerWhenFlashcardAlreadyHasAnswer() {
        flashcard.setAnswer("É uma sequência finita de instruções para resolver um problema.");

        when(flashcardRepository.findByIdAndDeckUserId(1L, 1L)).thenReturn(Optional.of(flashcard));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> flashcardService.generateFlashcardAnswer(1L, 1L)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verifyNoInteractions(flashcardAiService);
        verify(flashcardRepository, never()).save(any(Flashcard.class));
    }

    @Test
    void shouldNotGenerateAnswerWhenFlashcardDoesNotExist() {
        when(flashcardRepository.findByIdAndDeckUserId(99L, 1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> flashcardService.generateFlashcardAnswer(99L, 1L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verifyNoInteractions(flashcardAiService);
        verify(flashcardRepository, never()).save(any(Flashcard.class));
    }

    @Test
    void shouldNotGenerateAnswerForAnotherUsersFlashcard() {
        when(flashcardRepository.findByIdAndDeckUserId(1L, 2L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> flashcardService.generateFlashcardAnswer(1L, 2L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verifyNoInteractions(flashcardAiService);
        verify(flashcardRepository, never()).save(any(Flashcard.class));
    }

    @Test
    void shouldCreateFlashcardForOwnedDeck() {
        when(deckRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(deck));
        when(flashcardRepository.save(flashcard)).thenReturn(flashcard);

        Flashcard result = flashcardService.createFlashcard(1L, 1L, flashcard);

        assertEquals(flashcard, result);
        assertSame(deck, result.getDeck());

        verify(flashcardRepository).save(flashcard);
    }

    @Test
    void shouldNotCreateFlashcardForAnotherUsersDeck() {
        when(deckRepository.findByIdAndUserId(1L, 2L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> flashcardService.createFlashcard(2L, 1L, flashcard)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(flashcardRepository, never()).save(any(Flashcard.class));
    }

    @Test
    void shouldGetOnlyFlashcardsFromUser() {
        when(flashcardRepository.findByDeckUserId(1L)).thenReturn(List.of(flashcard));

        List<Flashcard> result = flashcardService.getAllFlashcards(1L);

        assertEquals(1, result.size());
        assertEquals(flashcard, result.get(0));
    }

    @Test
    void shouldNotGetAnotherUsersFlashcard() {
        when(flashcardRepository.findByIdAndDeckUserId(1L, 2L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> flashcardService.getFlashcardById(1L, 2L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void shouldGetFlashcardsFromOwnedDeck() {
        when(deckRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(deck));
        when(flashcardRepository.findByDeckIdAndDeckUserId(1L, 1L)).thenReturn(List.of(flashcard));

        List<Flashcard> result = flashcardService.getFlashcardsByDeckId(1L, 1L);

        assertEquals(1, result.size());
        assertEquals(flashcard, result.get(0));
    }

    @Test
    void shouldNotGetFlashcardsFromAnotherUsersDeck() {
        when(deckRepository.findByIdAndUserId(1L, 2L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> flashcardService.getFlashcardsByDeckId(1L, 2L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verifyNoInteractions(flashcardRepository);
    }

    @Test
    void shouldUpdateOwnedFlashcard() {
        Flashcard updatedFlashcard = new Flashcard();
        updatedFlashcard.setQuestion("O que caracteriza uma sequência numérica?");
        updatedFlashcard.setAnswer("É uma lista ordenada de números cujos termos " +
                "são determinados por uma regra de formação.");

        when(flashcardRepository.findByIdAndDeckUserId(1L, 1L)).thenReturn(Optional.of(flashcard));
        when(flashcardRepository.save(flashcard)).thenReturn(flashcard);

        Flashcard result = flashcardService.updateFlashcard(1L, 1L, updatedFlashcard);

        assertEquals(updatedFlashcard.getQuestion(), result.getQuestion());
        assertEquals(updatedFlashcard.getAnswer(), result.getAnswer());

        verify(flashcardRepository).save(flashcard);
    }

    @Test
    void shouldNotUpdateAnotherUsersFlashcard() {
        Flashcard updatedFlashcard = new Flashcard();
        updatedFlashcard.setQuestion("O que caracteriza uma sequência numérica?");

        when(flashcardRepository.findByIdAndDeckUserId(1L, 2L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> flashcardService.updateFlashcard(1L, 2L, updatedFlashcard)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(flashcardRepository, never()).save(any(Flashcard.class));
    }

    @Test
    void shouldDeleteOwnedFlashcard() {
        when(flashcardRepository.findByIdAndDeckUserId(1L, 1L)).thenReturn(Optional.of(flashcard));
        when(flashcardRepository.countByDeckId(1L)).thenReturn(1L);

        flashcardService.deleteFlashcard(1L, 1L);

        verify(flashcardRepository).delete(flashcard);
        verify(deckRepository, never()).delete(any(Deck.class));
    }

    @Test
    void shouldDeleteDeckWhenLastFlashcardIsDeleted() {
        when(flashcardRepository.findByIdAndDeckUserId(1L, 1L)).thenReturn(Optional.of(flashcard));
        when(flashcardRepository.countByDeckId(1L)).thenReturn(0L);

        flashcardService.deleteFlashcard(1L, 1L);

        verify(flashcardRepository).delete(flashcard);
        verify(deckRepository).delete(deck);
    }

    @Test
    void shouldNotDeleteAnotherUsersFlashcard() {
        when(flashcardRepository.findByIdAndDeckUserId(1L, 2L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> flashcardService.deleteFlashcard(1L, 2L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(flashcardRepository, never()).delete(any(Flashcard.class));
    }

    @Test
    void shouldNotCreateFlashcardWhileDeckIsInTraining() {
        deck.setTraining(true);

        Flashcard newFlashcard = new Flashcard();
        newFlashcard.setQuestion("O que caracteriza uma sequência numérica?");
        newFlashcard.setAnswer("É uma lista ordenada de números.");

        when(deckRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(deck));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> flashcardService.createFlashcard(1L, 1L, newFlashcard)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(flashcardRepository, never()).save(any(Flashcard.class));
    }

    @Test
    void shouldNotUpdateFlashcardWhileDeckIsInTraining() {
        deck.setTraining(true);

        Flashcard updatedFlashcard = new Flashcard();
        updatedFlashcard.setQuestion("Como um algoritmo pode ser definido?");
        updatedFlashcard.setAnswer("Como uma sequência finita de instruções.");

        when(flashcardRepository.findByIdAndDeckUserId(1L, 1L)).thenReturn(Optional.of(flashcard));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> flashcardService.updateFlashcard(1L, 1L, updatedFlashcard)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(flashcardRepository, never()).save(any(Flashcard.class));
    }

    @Test
    void shouldNotGenerateAnswerWhileDeckIsInTraining() {
        deck.setTraining(true);

        when(flashcardRepository.findByIdAndDeckUserId(1L, 1L)).thenReturn(Optional.of(flashcard));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> flashcardService.generateFlashcardAnswer(1L, 1L)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(flashcardAiService, never()).generateAnswer(any(), any());
    }

    @Test
    void shouldNotDeleteFlashcardWhileDeckIsInTraining() {
        deck.setTraining(true);

        when(flashcardRepository.findByIdAndDeckUserId(1L, 1L)).thenReturn(Optional.of(flashcard));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> flashcardService.deleteFlashcard(1L, 1L)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(flashcardRepository, never()).delete(any(Flashcard.class));
    }
}