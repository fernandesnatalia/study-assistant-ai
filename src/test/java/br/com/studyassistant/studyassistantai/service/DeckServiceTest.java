package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.dto.DeckAiCreateRequest;
import br.com.studyassistant.studyassistantai.dto.DeckCreateRequest;
import br.com.studyassistant.studyassistantai.dto.GeneratedFlashcard;
import br.com.studyassistant.studyassistantai.enums.SourceSelectionMode;
import br.com.studyassistant.studyassistantai.model.Deck;
import br.com.studyassistant.studyassistantai.model.Flashcard;
import br.com.studyassistant.studyassistantai.model.User;
import br.com.studyassistant.studyassistantai.enums.DeckMode;
import br.com.studyassistant.studyassistantai.repository.DeckRepository;
import br.com.studyassistant.studyassistantai.repository.FlashcardRepository;
import br.com.studyassistant.studyassistantai.repository.UserRepository;
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
import java.util.Set;
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
    private PerformanceService performanceService;

    @Mock
    private FlashcardAiService flashcardAiService;

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
        deck.setMode(DeckMode.STUDY);
    }

    @Test
    void shouldCreateDeckWithFirstFlashcard() {
        DeckCreateRequest request = new DeckCreateRequest();
        request.setTheme("Algoritmos");
        request.setQuestion("O que é um algoritmo?");
        request.setAnswer("É uma sequência finita de instruções para resolver um problema.");
        request.setMode(DeckMode.STUDY);

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
        assertEquals(DeckMode.STUDY, result.getMode());
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
    void shouldNotCreateTrainingDeckManually() {
        DeckCreateRequest request = new DeckCreateRequest();
        request.setTheme("Algoritmos");
        request.setQuestion("O que é um algoritmo?");
        request.setAnswer("Uma sequência de instruções.");
        request.setMode(DeckMode.TRAINING);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> deckService.create(request, 1L)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(deckRepository, never()).save(any(Deck.class));
    }

    @Test
    void shouldCreateDeckWithGeneratedFlashcards() {
        DeckAiCreateRequest request = new DeckAiCreateRequest(
                "Sequências Numéricas",
                DeckMode.STUDY,
                2,
                Set.of(),
                Set.of(),
                Set.of(),
                SourceSelectionMode.PREFERRED
        );

        List<GeneratedFlashcard> generatedFlashcards = List.of(
                new GeneratedFlashcard(
                        "O que é uma sequência numérica?",
                        "É uma lista ordenada de números definida por uma regra."
                ),
                new GeneratedFlashcard(
                        "O que é uma progressão aritmética?",
                        "É uma sequência em que a diferença entre termos consecutivos é constante."
                )
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        when(flashcardAiService.generateFlashcards("Sequências Numéricas", 2, ""))
                .thenReturn(generatedFlashcards);

        when(deckRepository.save(any(Deck.class)))
                .thenAnswer(invocation -> {
                    Deck savedDeck = invocation.getArgument(0);savedDeck.setId(1L);return savedDeck;}
                );

        Deck result = deckService.createWithAi(request, 1L);

        assertEquals(1L, result.getId());
        assertEquals("Sequências Numéricas", result.getTheme());
        assertEquals(DeckMode.STUDY, result.getMode());
        assertSame(user, result.getUser());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Flashcard>> flashcardsCaptor = ArgumentCaptor.forClass(List.class);

        verify(flashcardRepository).saveAll(flashcardsCaptor.capture());

        List<Flashcard> savedFlashcards = flashcardsCaptor.getValue();

        assertEquals(2, savedFlashcards.size());

        assertEquals(
                "O que é uma sequência numérica?",
                savedFlashcards.get(0).getQuestion()
        );

        assertEquals(
                "É uma lista ordenada de números definida por uma regra.",
                savedFlashcards.get(0).getAnswer()
        );

        assertEquals(
                "O que é uma progressão aritmética?",
                savedFlashcards.get(1).getQuestion()
        );

        assertEquals(
                "É uma sequência em que a diferença entre termos consecutivos é constante.",
                savedFlashcards.get(1).getAnswer()
        );

        assertSame(result, savedFlashcards.get(0).getDeck());
        assertSame(result, savedFlashcards.get(1).getDeck());
    }

    @Test
    void shouldCreateTrainingDeckWithAi() {
        DeckAiCreateRequest request = new DeckAiCreateRequest(
                "Algoritmos",
                DeckMode.TRAINING,
                1,
                Set.of(),
                Set.of(),
                Set.of(),
                SourceSelectionMode.PREFERRED
        );

        List<GeneratedFlashcard> generatedFlashcards = List.of(
                new GeneratedFlashcard("O que é um algoritmo?", "Uma sequência finita de instruções.")
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        when(flashcardAiService.generateFlashcards("Algoritmos", 1, ""))
                .thenReturn(generatedFlashcards);

        when(deckRepository.save(any(Deck.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Deck result = deckService.createWithAi(request, 1L);

        assertEquals(DeckMode.TRAINING, result.getMode());
        verify(flashcardRepository).saveAll(any());
    }

    @Test
    void shouldNotCreateDeckWhenAiGenerationFails() {
        DeckAiCreateRequest request = new DeckAiCreateRequest(
                "Sequências Numéricas", DeckMode.STUDY, 2, Set.of(), Set.of(), Set.of(), SourceSelectionMode.PREFERRED
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        when(flashcardAiService.generateFlashcards("Sequências Numéricas", 2, ""))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI returned an invalid flashcard format"));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> deckService.createWithAi(request, 1L)
        );

        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatusCode());

        verify(deckRepository, never()).save(any(Deck.class));
        verify(flashcardRepository, never()).saveAll(any());
    }

    @Test
    void shouldNotCreateDeckForNonexistentUser() {
        DeckCreateRequest request = new DeckCreateRequest();
        request.setTheme("Algoritmos");
        request.setQuestion("O que é um algoritmo?");
        request.setAnswer("É uma sequência finita de instruções para resolver um problema.");
        request.setMode(DeckMode.STUDY);

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
        assertEquals(DeckMode.STUDY, result.getMode());

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
    void shouldConsolidatePerformanceBeforeDeletingTrainingDeck() {
        deck.setMode(DeckMode.TRAINING);

        when(deckRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(deck));

        deckService.delete(1L, 1L);

        var inOrder = org.mockito.Mockito.inOrder(performanceService, deckRepository);

        inOrder.verify(performanceService).consolidateDeckPerformance(1L, 1L);
        inOrder.verify(deckRepository).delete(deck);
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
    void shouldConvertTrainingDeckToStudy() {
        deck.setMode(DeckMode.TRAINING);

        when(deckRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(deck));
        when(deckRepository.save(deck)).thenReturn(deck);

        Deck result = deckService.convertToStudy(1L, 1L);

        assertEquals(DeckMode.STUDY, result.getMode());

        verify(performanceService).consolidateDeckPerformance(1L, 1L);
        verify(deckRepository).save(deck);
    }

    @Test
    void shouldNotConvertStudyDeckToStudyAgain() {
        when(deckRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(deck));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> deckService.convertToStudy(1L, 1L)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(deckRepository, never()).save(any(Deck.class));
    }

    @Test
    void shouldNotConvertAnotherUsersDeck() {
        when(deckRepository.findByIdAndUserId(1L, 2L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> deckService.convertToStudy(1L, 2L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(deckRepository, never()).save(any(Deck.class));
    }
}