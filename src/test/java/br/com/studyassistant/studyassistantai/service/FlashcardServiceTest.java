package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.model.Content;
import br.com.studyassistant.studyassistantai.model.Flashcard;
import br.com.studyassistant.studyassistantai.repository.FlashcardRepository;
import br.com.studyassistant.studyassistantai.repository.ContentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
    private ContentRepository contentRepository;

    @Mock
    private GenerativeService generativeService;

    @InjectMocks
    private FlashcardService flashcardService;

    private Flashcard flashcard;
    private Content content;

    @BeforeEach
    void setUp() {
        content = new Content();
        content.setBody(
                "Um algoritmo descreve uma sequência finita de instruções para resolver um problema. " +
                "Sequências numéricas podem ser calculadas por procedimentos sistemáticos, como o " +
                "algoritmo de Ada Lovelace para os números de Bernoulli."
        );

        flashcard = new Flashcard();
        flashcard.setId(1L);
        flashcard.setQuestion("O que é um algoritmo?");
        flashcard.setContent(content);
    }

    @Test
    void shouldGenerateAnswerForFlashcardWithoutAnswer() {
        String generatedAnswer = "É uma sequência finita de instruções para resolver um problema.";

        when(flashcardRepository.findByIdAndContentUserId(1L, 1L)).thenReturn(Optional.of(flashcard));
        when(generativeService.generateAnswer(flashcard.getQuestion(), content.getBody())).thenReturn(generatedAnswer);
        when(flashcardRepository.save(flashcard)).thenReturn(flashcard);

        Flashcard result = flashcardService.generateFlashcardAnswer(1L, 1L);

        assertEquals(generatedAnswer, result.getAnswer());

        verify(generativeService).generateAnswer(flashcard.getQuestion(), content.getBody());
        verify(flashcardRepository).save(flashcard);
    }

    @Test
    void shouldNotGenerateAnswerWhenFlashcardAlreadyHasAnswer() {
        flashcard.setAnswer("É uma sequência finita de instruções para resolver um problema.");

        when(flashcardRepository.findByIdAndContentUserId(1L, 1L)).thenReturn(Optional.of(flashcard));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> flashcardService.generateFlashcardAnswer(1L, 1L)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verifyNoInteractions(generativeService);
        verify(flashcardRepository, never()).save(any(Flashcard.class));
    }

    @Test
    void shouldNotGenerateAnswerWhenFlashcardDoesNotExist() {
        when(flashcardRepository.findByIdAndContentUserId(99L, 1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> flashcardService.generateFlashcardAnswer(99L, 1L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verifyNoInteractions(generativeService);
        verify(flashcardRepository, never()).save(any(Flashcard.class));
    }

    @Test
    void shouldNotGenerateAnswerForAnotherUsersFlashcard() {
        when(flashcardRepository.findByIdAndContentUserId(1L, 2L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> flashcardService.generateFlashcardAnswer(1L, 2L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verifyNoInteractions(generativeService);
        verify(flashcardRepository, never()).save(any(Flashcard.class));
    }

    @Test
    void shouldCreateFlashcardForOwnedContent() {
        when(contentRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(content));
        when(flashcardRepository.save(flashcard)).thenReturn(flashcard);

        Flashcard result = flashcardService.createFlashcard(1L, 1L, flashcard);

        assertEquals(flashcard, result);
        assertEquals(content, result.getContent());

        verify(flashcardRepository).save(flashcard);
    }

    @Test
    void shouldNotCreateFlashcardForAnotherUsersContent() {
        when(contentRepository.findByIdAndUserId(1L, 2L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> flashcardService.createFlashcard(2L, 1L, flashcard)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(flashcardRepository, never()).save(any(Flashcard.class));
    }

    @Test
    void shouldGetOnlyFlashcardsFromUser() {
        when(flashcardRepository.findByContentUserId(1L)).thenReturn(List.of(flashcard));

        List<Flashcard> result = flashcardService.getAllFlashcards(1L);

        assertEquals(1, result.size());
        assertEquals(flashcard, result.get(0));
    }

    @Test
    void shouldNotGetAnotherUsersFlashcard() {
        when(flashcardRepository.findByIdAndContentUserId(1L, 2L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> flashcardService.getFlashcardById(1L, 2L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void shouldGetFlashcardsFromOwnedContent() {
        when(contentRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(content));
        when(flashcardRepository.findByContentIdAndContentUserId(1L, 1L)).thenReturn(List.of(flashcard));

        List<Flashcard> result = flashcardService.getFlashcardsByContentId(1L, 1L);

        assertEquals(1, result.size());
        assertEquals(flashcard, result.get(0));
    }

    @Test
    void shouldNotGetFlashcardsFromAnotherUsersContent() {
        when(contentRepository.findByIdAndUserId(1L, 2L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> flashcardService.getFlashcardsByContentId(1L, 2L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verifyNoInteractions(flashcardRepository);
    }

    @Test
    void shouldUpdateOwnedFlashcard() {
        Flashcard updatedFlashcard = new Flashcard();
        updatedFlashcard.setQuestion("O que caracteriza uma sequência numérica?");
        updatedFlashcard.setAnswer("É uma lista ordenada de números cujos termos são determinados por uma regra de formação.");

        when(flashcardRepository.findByIdAndContentUserId(1L, 1L)).thenReturn(Optional.of(flashcard));
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

        when(flashcardRepository.findByIdAndContentUserId(1L, 2L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> flashcardService.updateFlashcard(1L, 2L, updatedFlashcard)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(flashcardRepository, never()).save(any(Flashcard.class));
    }

    @Test
    void shouldDeleteOwnedFlashcard() {
        when(flashcardRepository.findByIdAndContentUserId(1L, 1L)).thenReturn(Optional.of(flashcard));

        flashcardService.deleteFlashcard(1L, 1L);

        verify(flashcardRepository).delete(flashcard);
    }

    @Test
    void shouldNotDeleteAnotherUsersFlashcard() {
        when(flashcardRepository.findByIdAndContentUserId(1L, 2L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> flashcardService.deleteFlashcard(1L, 2L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(flashcardRepository, never()).delete(any(Flashcard.class));
    }

}