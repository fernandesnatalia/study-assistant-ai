package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.dto.ContentGenerationRequest;
import br.com.studyassistant.studyassistantai.dto.GeneratedContentResponse;
import br.com.studyassistant.studyassistantai.model.Topic;
import br.com.studyassistant.studyassistantai.repository.TopicRepository;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContentAiServiceTest {

    @Mock
    private GenerativeService generativeService;

    @Mock
    private TopicRepository topicRepository;

    @InjectMocks
    private ContentAiService contentAiService;

    @Test
    void shouldGenerateContentForOwnedTopic() {
        Topic topic = new Topic();
        topic.setId(1L);
        topic.setName("Teoria dos Anéis");

        ContentGenerationRequest request = new ContentGenerationRequest(
                1L,
                "Explique o conceito de anel comutativo.",
                "Considere a definição de anel e a propriedade comutativa da multiplicação."
        );

        String generatedContent = """
                TÍTULO: Anéis Comutativos
                CONTEÚDO:
                Um anel comutativo é um anel em que a operação de multiplicação é comutativa.
                """;

        when(topicRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(topic));
        when(generativeService.generate(anyString(), anyString())).thenReturn(generatedContent);

        GeneratedContentResponse result = contentAiService.generateContent(1L, request);

        assertEquals(1L, result.topicId());
        assertEquals("Anéis Comutativos", result.title());
        assertEquals("Um anel comutativo é um anel em que a operação de multiplicação é comutativa.", result.body());

        verify(topicRepository).findByIdAndUserId(1L, 1L);
        verify(generativeService).generate(anyString(), anyString());
    }

    @Test
    void shouldGenerateContentWithoutAdditionalContext() {
        Topic topic = new Topic();
        topic.setId(1L);
        topic.setName("Teoria dos Anéis");

        ContentGenerationRequest request = new ContentGenerationRequest(1L, "Explique o conceito de anel.", null);

        String generatedContent = """
                TÍTULO: Introdução aos Anéis
                CONTEÚDO:
                Um anel é uma estrutura algébrica definida por duas operações.
                """;

        when(topicRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(topic));
        when(generativeService.generate(anyString(), anyString())).thenReturn(generatedContent);

        GeneratedContentResponse result = contentAiService.generateContent(1L, request);

        assertEquals("Introdução aos Anéis", result.title());
        assertEquals("Um anel é uma estrutura algébrica definida por duas operações.", result.body());
    }

    @Test
    void shouldNotGenerateContentForAnotherUsersTopic() {
        ContentGenerationRequest request = new ContentGenerationRequest(1L, "Explique o conceito de anel.", null);

        when(topicRepository.findByIdAndUserId(1L, 2L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> contentAiService.generateContent(2L, request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(generativeService, never()).generate(anyString(), anyString());
    }

    @Test
    void shouldRejectInvalidAiResponse() {
        Topic topic = new Topic();
        topic.setId(1L);
        topic.setName("Teoria dos Anéis");

        ContentGenerationRequest request = new ContentGenerationRequest(1L, "Explique o conceito de anel.", null);

        when(topicRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(topic));
        when(generativeService.generate(anyString(), anyString())).thenReturn("Resposta fora do formato esperado.");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> contentAiService.generateContent(1L, request)
        );

        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatusCode());
    }
}