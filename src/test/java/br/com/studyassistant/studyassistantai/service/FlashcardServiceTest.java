package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.dto.GeneratedFlashcard;
import br.com.studyassistant.studyassistantai.model.Content;
import br.com.studyassistant.studyassistantai.model.Deck;
import br.com.studyassistant.studyassistantai.model.Flashcard;
import br.com.studyassistant.studyassistantai.model.Topic;
import br.com.studyassistant.studyassistantai.model.DeckMode;
import br.com.studyassistant.studyassistantai.model.User;
import br.com.studyassistant.studyassistantai.repository.ContentRepository;
import br.com.studyassistant.studyassistantai.repository.DeckRepository;
import br.com.studyassistant.studyassistantai.repository.FlashcardRepository;
import br.com.studyassistant.studyassistantai.repository.TopicRepository;
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
    private ContentRepository contentRepository;

    @Mock
    private TopicRepository topicRepository;

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
        deck.setMode(DeckMode.STUDY);

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
    void shouldGenerateFlashcardsWithoutAdditionalContext() {
        GeneratedFlashcard generatedFlashcard = new GeneratedFlashcard(
                "O que é um algoritmo?",
                "É uma sequência finita de instruções para resolver um problema."
        );

        Flashcard savedFlashcard = new Flashcard();
        savedFlashcard.setQuestion(generatedFlashcard.question());
        savedFlashcard.setAnswer(generatedFlashcard.answer());
        savedFlashcard.setDeck(deck);

        when(deckRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(deck));
        when(flashcardAiService.generateFlashcards("Algoritmos", 1, ""))
                .thenReturn(List.of(generatedFlashcard));
        when(flashcardRepository.saveAll(any())).thenReturn(List.of(savedFlashcard));

        List<Flashcard> result = flashcardService.generateFlashcards(
                1L, 1L, 1, null, null, null
        );

        assertEquals(1, result.size());
        assertEquals(generatedFlashcard.question(), result.get(0).getQuestion());
        assertEquals(generatedFlashcard.answer(), result.get(0).getAnswer());
        assertSame(deck, result.get(0).getDeck());

        verify(flashcardAiService).generateFlashcards("Algoritmos", 1, "");
        verify(flashcardRepository).saveAll(any());
    }

    @Test
    void shouldGenerateFlashcardsUsingOwnedContentAsContext() {
        Content content = new Content();
        content.setId(1L);
        content.setTitle("Algoritmos e Máquinas Analíticas");
        content.setBody("Um algoritmo descreve uma sequência finita de instruções para resolver um problema.");

        String expectedContext = """
                Conteúdo de estudo selecionado:
                Título: Algoritmos e Máquinas Analíticas
                Conteúdo: Um algoritmo descreve uma sequência finita de instruções para resolver um problema.
                """.trim();

        GeneratedFlashcard generatedFlashcard = new GeneratedFlashcard(
                "O que é um algoritmo?",
                "É uma sequência finita de instruções para resolver um problema."
        );

        when(deckRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(deck));
        when(contentRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(content));
        when(flashcardAiService.generateFlashcards("Algoritmos", 1, expectedContext))
                .thenReturn(List.of(generatedFlashcard));
        when(flashcardRepository.saveAll(any())).thenReturn(List.of(flashcard));

        flashcardService.generateFlashcards(
                1L, 1L, 1, 1L, null, null
        );

        verify(contentRepository).findByIdAndUserId(1L, 1L);
        verify(flashcardAiService).generateFlashcards("Algoritmos", 1, expectedContext);
        verify(flashcardRepository).saveAll(any());
    }

    @Test
    void shouldGenerateFlashcardsUsingOwnedTopicAsContext() {
        Topic topic = new Topic();
        topic.setId(1L);
        topic.setName("Algoritmos Matemáticos");

        String expectedContext = """
                Tópico selecionado:
                Algoritmos Matemáticos
                """.trim();

        GeneratedFlashcard generatedFlashcard = new GeneratedFlashcard(
                "O que é um algoritmo?",
                "É uma sequência finita de instruções para resolver um problema."
        );

        when(deckRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(deck));
        when(topicRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(topic));
        when(flashcardAiService.generateFlashcards("Algoritmos", 1, expectedContext))
                .thenReturn(List.of(generatedFlashcard));
        when(flashcardRepository.saveAll(any())).thenReturn(List.of(flashcard));

        flashcardService.generateFlashcards(
                1L, 1L, 1, null, 1L, null
        );

        verify(topicRepository).findByIdAndUserId(1L, 1L);
        verify(flashcardAiService).generateFlashcards("Algoritmos", 1, expectedContext);
        verify(flashcardRepository).saveAll(any());
    }

    @Test
    void shouldGenerateFlashcardsUsingProvidedTextContext() {
        String context = "Priorize conceitos relacionados à representação de procedimentos matemáticos.";

        String expectedContext = """
                Contexto adicional fornecido pelo usuário:
                Priorize conceitos relacionados à representação de procedimentos matemáticos.
                """.trim();

        GeneratedFlashcard generatedFlashcard = new GeneratedFlashcard(
                "Como algoritmos representam procedimentos matemáticos?",
                "Por meio de sequências definidas de instruções e operações."
        );

        when(deckRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(deck));
        when(flashcardAiService.generateFlashcards("Algoritmos", 1, expectedContext))
                .thenReturn(List.of(generatedFlashcard));
        when(flashcardRepository.saveAll(any())).thenReturn(List.of(flashcard));

        flashcardService.generateFlashcards(
                1L, 1L, 1, null, null, context
        );

        verify(flashcardAiService).generateFlashcards("Algoritmos", 1, expectedContext);
        verify(flashcardRepository).saveAll(any());
    }

    @Test
    void shouldNotGenerateFlashcardsUsingAnotherUsersContent() {
        when(deckRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(deck));
        when(contentRepository.findByIdAndUserId(2L, 1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> flashcardService.generateFlashcards(
                        1L, 1L, 1, 2L, null, null
                )
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(contentRepository).findByIdAndUserId(2L, 1L);
        verifyNoInteractions(flashcardAiService);
        verify(flashcardRepository, never()).saveAll(any());
    }

    @Test
    void shouldNotGenerateFlashcardsUsingAnotherUsersTopic() {
        when(deckRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(deck));
        when(topicRepository.findByIdAndUserId(2L, 1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> flashcardService.generateFlashcards(
                        1L, 1L, 1, null, 2L, null
                )
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(topicRepository).findByIdAndUserId(2L, 1L);
        verifyNoInteractions(flashcardAiService);
        verify(flashcardRepository, never()).saveAll(any());
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
        updatedFlashcard.setAnswer(
                "É uma lista ordenada de números cujos termos são determinados por uma regra de formação."
        );

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
        deck.setMode(DeckMode.TRAINING);

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
        deck.setMode(DeckMode.TRAINING);

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
        deck.setMode(DeckMode.TRAINING);

        when(flashcardRepository.findByIdAndDeckUserId(1L, 1L)).thenReturn(Optional.of(flashcard));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> flashcardService.generateFlashcardAnswer(1L, 1L)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(flashcardAiService, never()).generateAnswer(any(), any());
    }

    @Test
    void shouldNotDeleteFlashcardWhileDeckIsInTraining() {
        deck.setMode(DeckMode.TRAINING);

        when(flashcardRepository.findByIdAndDeckUserId(1L, 1L)).thenReturn(Optional.of(flashcard));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> flashcardService.deleteFlashcard(1L, 1L)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(flashcardRepository, never()).delete(any(Flashcard.class));
    }
}