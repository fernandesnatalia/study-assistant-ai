package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.dto.ContentGenerationRequest;
import br.com.studyassistant.studyassistantai.dto.GeneratedContentResponse;
import br.com.studyassistant.studyassistantai.dto.RagContext;
import br.com.studyassistant.studyassistantai.dto.RetrievedSource;
import br.com.studyassistant.studyassistantai.dto.SemanticSearchRequest;
import br.com.studyassistant.studyassistantai.enums.SourceSelectionMode;
import br.com.studyassistant.studyassistantai.model.Topic;
import br.com.studyassistant.studyassistantai.prompt.AiPromptBuilder;
import br.com.studyassistant.studyassistantai.prompt.RagPromptBuilder;
import br.com.studyassistant.studyassistantai.repository.TopicRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Optional;
import static br.com.studyassistant.studyassistantai.constants.ErrorMessages.INSUFFICIENT_SOURCES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ContentAiServiceTest {

    @Mock
    private GenerativeService generativeService;

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private RagContextService ragContextService;

    @InjectMocks
    private ContentAiService contentAiService;

    @Spy
    private RagPromptBuilder ragPromptBuilder = new RagPromptBuilder();

    @Spy
    private AiPromptBuilder aiPromptBuilder = new AiPromptBuilder();

    private static final String TEXT = "Trecho sobre anéis comutativos.";
    public static final String TITLE = "Anéis Comutativos";
    public static final String REQUEST = "Explique o conceito de anel.";

    @Test
    void shouldGenerateContentForOwnedTopic() {
        ContentGenerationRequest request = new ContentGenerationRequest(
                1L,
                "Explique o conceito de anel comutativo.",
                "Considere a definição de anel e a propriedade comutativa da multiplicação.",
                null
        );

        when(topicRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(topic()));
        when(generativeService.generate(anyString(), anyString())).thenReturn(validGeneratedContent());

        GeneratedContentResponse result = contentAiService.generateContent(1L, request);

        assertEquals(1L, result.topicId());
        assertEquals(TITLE, result.title());
        assertEquals("Um anel comutativo possui multiplicação comutativa.", result.body());
        assertTrue(result.sources().isEmpty());

        verify(topicRepository).findByIdAndUserId(1L, 1L);
        verify(generativeService).generate(anyString(), anyString());
        verifyNoInteractions(ragContextService);
    }

    @Test
    void shouldGenerateContentWithoutAdditionalContext() {
        ContentGenerationRequest request = new ContentGenerationRequest(1L, REQUEST, null, null);

        when(topicRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(topic()));
        when(generativeService.generate(anyString(), anyString())).thenReturn(validGeneratedContent());

        GeneratedContentResponse result = contentAiService.generateContent(1L, request);

        assertEquals(TITLE, result.title());
        assertTrue(result.sources().isEmpty());

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);

        verify(generativeService).generate(anyString(), promptCaptor.capture());

        assertTrue(promptCaptor.getValue().contains("Nenhum contexto adicional fornecido."));
        assertFalse(promptCaptor.getValue().contains("Fontes recuperadas:"));
    }

    @Test
    void shouldNotGenerateContentForAnotherUsersTopic() {
        ContentGenerationRequest request = new ContentGenerationRequest(1L, REQUEST, null, null);

        when(topicRepository.findByIdAndUserId(1L, 2L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> contentAiService.generateContent(2L, request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verifyNoInteractions(ragContextService);
        verify(generativeService, never()).generate(anyString(), anyString());
    }

    @Test
    void shouldRejectInvalidAiResponse() {
        ContentGenerationRequest request = new ContentGenerationRequest(1L, REQUEST, null, null);

        when(topicRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(topic()));
        when(generativeService.generate(anyString(), anyString())).thenReturn("Resposta fora do formato esperado.");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> contentAiService.generateContent(1L, request)
        );

        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatusCode());
    }

    @Test
    void shouldGenerateContentWithRetrievedSources() {
        SemanticSearchRequest search = search(SourceSelectionMode.PREFERRED);
        ContentGenerationRequest request = generationRequest(search);

        RetrievedSource source = source();

        when(topicRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(topic()));

        when(ragContextService.build(1L, search))
                .thenReturn(new RagContext("Fonte 1\nTrecho sobre anéis comutativos.", List.of(source)));

        when(generativeService.generate(anyString(), anyString())).thenReturn(validGeneratedContent());

        GeneratedContentResponse result = contentAiService.generateContent(1L, request);

        assertEquals(TITLE, result.title());
        assertEquals(List.of(source), result.sources());

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);

        verify(generativeService).generate(anyString(), promptCaptor.capture());

        assertTrue(promptCaptor.getValue().contains(TEXT));

        verify(ragContextService).build(1L, search);
    }

    @Test
    void shouldAllowGenerationWithoutSourcesInPreferredMode() {
        SemanticSearchRequest search = search(SourceSelectionMode.PREFERRED);
        ContentGenerationRequest request = generationRequest(search);

        when(topicRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(topic()));
        when(ragContextService.build(1L, search)).thenReturn(new RagContext("", List.of()));
        when(generativeService.generate(anyString(), anyString())).thenReturn(validGeneratedContent());

        GeneratedContentResponse result = contentAiService.generateContent(1L, request);

        assertEquals(TITLE, result.title());
        assertTrue(result.sources().isEmpty());

        verify(generativeService).generate(anyString(), anyString());
    }

    @Test
    void shouldUseExclusiveSourceInstructions() {
        SemanticSearchRequest search = search(SourceSelectionMode.EXCLUSIVE);
        ContentGenerationRequest request = generationRequest(search);

        when(topicRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(topic()));

        when(ragContextService.build(1L, search))
                .thenReturn(new RagContext("Fonte 1\nTrecho sobre anéis comutativos.", List.of(source())));

        when(generativeService.generate(anyString(), anyString())).thenReturn(validGeneratedContent());

        contentAiService.generateContent(1L, request);

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);

        verify(generativeService).generate(anyString(), promptCaptor.capture());

        assertTrue(promptCaptor.getValue().contains("Utilize exclusivamente as fontes recuperadas"));
    }

    @Test
    void shouldNotGenerateWhenExclusiveSourcesAreMissing() {
        SemanticSearchRequest search = search(SourceSelectionMode.EXCLUSIVE);
        ContentGenerationRequest request = generationRequest(search);

        when(topicRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(topic()));

        when(ragContextService.build(1L, search)).thenThrow(new ResponseStatusException(
                HttpStatus.UNPROCESSABLE_CONTENT, INSUFFICIENT_SOURCES));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> contentAiService.generateContent(1L, request));

        assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, exception.getStatusCode());

        verify(generativeService, never()).generate(anyString(), anyString());
    }

    private Topic topic() {
        Topic topic = new Topic();
        topic.setId(1L);
        topic.setName("Teoria dos Anéis");
        return topic;
    }

    private ContentGenerationRequest generationRequest(
            SemanticSearchRequest search
    ) {
        return new ContentGenerationRequest(1L, "Explique os anéis comutativos.", null, search);
    }

    private SemanticSearchRequest search(SourceSelectionMode mode) {
        return new SemanticSearchRequest(TITLE, null, null, null, mode, 5, 0.7);
    }

    private RetrievedSource source() {
        return new RetrievedSource(
                "chunk-1",
                0,
                TEXT,
                0.95,
                10L,
                "Anéis",
                1L,
                "Teoria dos Anéis",
                1L,
                "Matemática",
                null,
                null
        );
    }

    private String validGeneratedContent() {
        return """
                TÍTULO: Anéis Comutativos
                CONTEÚDO:
                Um anel comutativo possui multiplicação comutativa.
                """;
    }
}