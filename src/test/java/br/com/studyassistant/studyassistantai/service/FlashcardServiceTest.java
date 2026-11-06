package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.model.Content;
import br.com.studyassistant.studyassistantai.model.Flashcard;
import br.com.studyassistant.studyassistantai.repository.ContentRepository;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlashcardServiceTest {

    @Mock
    private FlashcardRepository flashcardRepository;

    @Mock
    private ContentRepository contentRepository;

    @InjectMocks
    private FlashcardService flashcardService;

    private Flashcard flashcard;

    private Content content;

    @BeforeEach
    void setUp() {
        content = new Content();
        content.setId(1L);

        flashcard = new Flashcard();
        flashcard.setId(1L);
        flashcard.setQuestion("O que é uma equação do segundo grau?");
        flashcard.setAnswer(null);
        flashcard.setContent(content);
    }

    @Test
    void shouldCreateFlashcardWithoutAnswer() {
        when(contentRepository.findById(1L)).thenReturn(Optional.of(content));
        when(flashcardRepository.save(any(Flashcard.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Flashcard result = flashcardService.createFlashcard(1L, flashcard);

        assertEquals("O que é uma equação do segundo grau?", result.getQuestion());
        assertNull(result.getAnswer());
        assertEquals(content, result.getContent());
        assertNull(result.getId());

        verify(contentRepository).findById(1L);
        verify(flashcardRepository).save(flashcard);
    }

    @Test
    void shouldCreateFlashcardWithAnswer() {
        flashcard.setAnswer("É uma equação da forma ax² + bx + c = 0, com a diferente de zero.");

        when(contentRepository.findById(1L)).thenReturn(Optional.of(content));
        when(flashcardRepository.save(any(Flashcard.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Flashcard result = flashcardService.createFlashcard(1L, flashcard);

        assertEquals("É uma equação da forma ax² + bx + c = 0, com a diferente de zero.", result.getAnswer());

        assertEquals(content, result.getContent());

        verify(flashcardRepository).save(flashcard);
    }

    @Test
    void shouldThrowWhenContentNotFound() {
        when(contentRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> flashcardService.createFlashcard(99L, flashcard)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(contentRepository).findById(99L);
        verify(flashcardRepository, never()).save(any(Flashcard.class));
    }

    @Test
    void shouldGetAllFlashcards() {
        when(flashcardRepository.findAll()).thenReturn(List.of(flashcard));

        List<Flashcard> result = flashcardService.getAllFlashcards();

        assertEquals(1, result.size());
        assertEquals(flashcard, result.get(0));

        verify(flashcardRepository).findAll();
    }

    @Test
    void shouldGetFlashcardById() {
        when(flashcardRepository.findById(1L)).thenReturn(Optional.of(flashcard));

        Flashcard result = flashcardService.getFlashcardById(1L);

        assertEquals(1L, result.getId());
        assertEquals("O que é uma equação do segundo grau?", result.getQuestion());

        verify(flashcardRepository).findById(1L);
    }

    @Test
    void shouldThrowWhenFlashcardNotFound() {
        when(flashcardRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> flashcardService.getFlashcardById(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(flashcardRepository).findById(99L);
    }

    @Test
    void shouldGetFlashcardsByContentId() {
        when(contentRepository.existsById(1L)).thenReturn(true);
        when(flashcardRepository.findByContentId(1L)).thenReturn(List.of(flashcard));

        List<Flashcard> result = flashcardService.getFlashcardsByContentId(1L);

        assertEquals(1, result.size());
        assertEquals(flashcard, result.get(0));
        assertEquals(1L, result.get(0).getContent().getId());

        verify(contentRepository).existsById(1L);
        verify(flashcardRepository).findByContentId(1L);
    }

    @Test
    void shouldUpdateFlashcard() {
        Flashcard updatedFlashcard = new Flashcard();

        updatedFlashcard.setQuestion("Qual é a forma geral de uma equação do segundo grau?");
        updatedFlashcard.setAnswer("ax² + bx + c = 0, em que a, b e c são coeficientes reais e a é diferente de zero.");

        when(flashcardRepository.findById(1L)).thenReturn(Optional.of(flashcard));
        when(flashcardRepository.save(any(Flashcard.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Flashcard result = flashcardService.updateFlashcard(1L, updatedFlashcard);

        assertEquals("Qual é a forma geral de uma equação do segundo grau?", result.getQuestion());
        assertEquals("ax² + bx + c = 0, em que a, b e c são coeficientes reais e a é diferente de zero.", result.getAnswer());

        assertEquals(content, result.getContent());

        verify(flashcardRepository).findById(1L);
        verify(flashcardRepository).save(flashcard);
    }

    @Test
    void shouldDeleteFlashcard() {
        when(flashcardRepository.findById(1L)).thenReturn(Optional.of(flashcard));

        flashcardService.deleteFlashcard(1L);

        verify(flashcardRepository).findById(1L);
        verify(flashcardRepository).delete(flashcard);
    }

    @Test
    void shouldReturnEmptyListWhenContentHasNoFlashcards() {
        when(contentRepository.existsById(1L)).thenReturn(true);
        when(flashcardRepository.findByContentId(1L)).thenReturn(List.of());

        List<Flashcard> result = flashcardService.getFlashcardsByContentId(1L);

        assertTrue(result.isEmpty());

        verify(flashcardRepository).findByContentId(1L);
    }

    @Test
    void shouldThrowWhenListingFlashcardsForNonexistentContent() {
        when(contentRepository.existsById(99L)).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> flashcardService.getFlashcardsByContentId(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(contentRepository).existsById(99L);
        verify(flashcardRepository, never()).findByContentId(99L);
    }

    @Test
    void shouldThrowWhenUpdatingNonexistentFlashcard() {
        Flashcard updatedFlashcard = new Flashcard();
        updatedFlashcard.setQuestion("Nova pergunta");

        when(flashcardRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> flashcardService.updateFlashcard(99L, updatedFlashcard)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(flashcardRepository, never()).save(any(Flashcard.class));
    }

    @Test
    void shouldThrowWhenDeletingNonexistentFlashcard() {
        when(flashcardRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> flashcardService.deleteFlashcard(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(flashcardRepository, never()).delete(any(Flashcard.class));
    }
}