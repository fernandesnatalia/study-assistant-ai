package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.model.Content;
import br.com.studyassistant.studyassistantai.model.Flashcard;
import br.com.studyassistant.studyassistantai.repository.FlashcardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;
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
    private GenerativeService generativeService;

    @InjectMocks
    private FlashcardService flashcardService;

    private Flashcard flashcard;
    private Content content;

    @BeforeEach
    void setUp() {
        content = new Content();
        content.setBody("Uma equação do segundo grau possui a forma ax² + bx + c = 0, com a diferente de zero.");

        flashcard = new Flashcard();
        flashcard.setId(1L);
        flashcard.setQuestion("O que é uma equação do segundo grau?");
        flashcard.setContent(content);
    }

    @Test
    void shouldGenerateAnswerForFlashcardWithoutAnswer() {
        String generatedAnswer = "É uma equação polinomial de grau dois.";

        when(flashcardRepository.findById(1L)).thenReturn(Optional.of(flashcard));

        when(generativeService.generateAnswer(
                flashcard.getQuestion(),
                content.getBody()
        )).thenReturn(generatedAnswer);

        when(flashcardRepository.save(flashcard)).thenReturn(flashcard);

        Flashcard result = flashcardService.generateFlashcardAnswer(1L);

        assertEquals(generatedAnswer, result.getAnswer());

        verify(generativeService).generateAnswer(flashcard.getQuestion(), content.getBody());
        verify(flashcardRepository).save(flashcard);
    }

    @Test
    void shouldNotGenerateAnswerWhenFlashcardAlreadyHasAnswer() {
        flashcard.setAnswer("É uma equação polinomial de grau dois.");

        when(flashcardRepository.findById(1L)).thenReturn(Optional.of(flashcard));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> flashcardService.generateFlashcardAnswer(1L)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verifyNoInteractions(generativeService);
        verify(flashcardRepository, never()).save(any(Flashcard.class));
    }

    @Test
    void shouldNotGenerateAnswerWhenFlashcardDoesNotExist() {
        when(flashcardRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> flashcardService.generateFlashcardAnswer(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verifyNoInteractions(generativeService);
        verify(flashcardRepository, never()).save(any(Flashcard.class));
    }
}