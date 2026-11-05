package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.model.Content;
import br.com.studyassistant.studyassistantai.model.Topic;
import br.com.studyassistant.studyassistantai.repository.ContentRepository;
import br.com.studyassistant.studyassistantai.repository.TopicRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContentServiceTest {

    @Mock
    private ContentRepository contentRepository;

    @Mock
    private TopicRepository topicRepository;

    @InjectMocks
    private ContentService contentService;

    @Test
    void shouldCreateContentWithExistingTopic() {
        Topic topic = createTopic();
        Content content = createContent(topic);

        when(topicRepository.findById(1L)).thenReturn(Optional.of(topic));
        when(contentRepository.save(content)).thenReturn(content);

        Content result = contentService.createContent(content);

        assertSame(content, result);
        assertSame(topic, result.getTopic());

        verify(topicRepository).findById(1L);
        verify(contentRepository).save(content);
    }

    @Test
    void shouldThrowBadRequestWhenTopicIdIsNull() {
        Topic topic = new Topic();
        Content content = createContent(topic);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> contentService.createContent(content)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verifyNoInteractions(contentRepository, topicRepository);
    }

    @Test
    void shouldThrowNotFoundWhenCreatingContentWithNonexistentTopic() {
        Content content = createContent(createTopic());

        when(topicRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> contentService.createContent(content)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(contentRepository, never()).save(any());
    }

    @Test
    void shouldGetAllContents() {
        Content content = createContent(createTopic());

        when(contentRepository.findAll()).thenReturn(List.of(content));

        List<Content> result = contentService.getAllContents();

        assertEquals(1, result.size());
        assertSame(content, result.get(0));

        verify(contentRepository).findAll();
    }

    @Test
    void shouldGetContentById() {
        Content content = createContent(createTopic());

        when(contentRepository.findById(1L)).thenReturn(Optional.of(content));

        Content result = contentService.getContentById(1L);

        assertSame(content, result);
        verify(contentRepository).findById(1L);
    }

    @Test
    void shouldThrowNotFoundWhenContentDoesNotExist() {
        when(contentRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> contentService.getContentById(1L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void shouldUpdateContent() {
        Content existingContent = createContent(createTopic());

        Content updatedContent = new Content();
        updatedContent.setTitle("Fórmula de Bhaskara");
        updatedContent.setBody("O discriminante é Δ = b² - 4ac.");

        when(contentRepository.findById(1L)).thenReturn(Optional.of(existingContent));
        when(contentRepository.save(existingContent)).thenReturn(existingContent);

        Content result = contentService.updateContent(1L, updatedContent);

        assertEquals("Fórmula de Bhaskara", result.getTitle());
        assertEquals("O discriminante é Δ = b² - 4ac.", result.getBody());
        assertEquals(1L, result.getTopic().getId());

        verify(contentRepository).save(existingContent);
    }

    @Test
    void shouldThrowBadRequestWhenUpdatingWithBlankTitle() {
        Content existingContent = createContent(createTopic());

        Content updatedContent = new Content();
        updatedContent.setTitle("");
        updatedContent.setBody("Conteúdo válido.");

        when(contentRepository.findById(1L)).thenReturn(Optional.of(existingContent));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> contentService.updateContent(1L, updatedContent)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(contentRepository, never()).save(any());
    }

    @Test
    void shouldThrowBadRequestWhenUpdatingWithBlankBody() {
        Content existingContent = createContent(createTopic());

        Content updatedContent = new Content();
        updatedContent.setTitle("Equações do Segundo Grau");
        updatedContent.setBody("");

        when(contentRepository.findById(1L)).thenReturn(Optional.of(existingContent));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> contentService.updateContent(1L, updatedContent)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(contentRepository, never()).save(any());
    }

    @Test
    void shouldThrowNotFoundWhenUpdatingNonexistentContent() {
        Content updatedContent = createContent(createTopic());

        when(contentRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> contentService.updateContent(1L, updatedContent)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(contentRepository, never()).save(any());
    }

    @Test
    void shouldDeleteExistingContent() {
        Content content = createContent(createTopic());

        when(contentRepository.findById(1L)).thenReturn(Optional.of(content));

        contentService.deleteContent(1L);

        verify(contentRepository).delete(content);
    }

    @Test
    void shouldThrowNotFoundWhenDeletingNonexistentContent() {
        when(contentRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> contentService.deleteContent(1L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(contentRepository, never()).delete(any());
    }

    private Topic createTopic() {
        Topic topic = new Topic();
        topic.setId(1L);
        topic.setName("Equações do Segundo Grau");
        return topic;
    }

    private Content createContent(Topic topic) {
        Content content = new Content();
        content.setId(1L);
        content.setTitle("Introdução às Equações do Segundo Grau");
        content.setBody("Uma equação do segundo grau possui a forma ax² + bx + c = 0.");
        content.setTopic(topic);
        return content;
    }
}
