package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.model.Content;
import br.com.studyassistant.studyassistantai.model.Topic;
import br.com.studyassistant.studyassistantai.model.User;
import br.com.studyassistant.studyassistantai.repository.ContentRepository;
import br.com.studyassistant.studyassistantai.repository.TopicRepository;
import br.com.studyassistant.studyassistantai.repository.UserRepository;
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
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ContentServiceTest {

    @Mock
    private ContentRepository contentRepository;

    @Mock
    private TopicRepository topicRepository;

    @InjectMocks
    private ContentService contentService;

    @Mock
    private UserRepository userRepository;

    private Topic createTopic() {
        Topic topic = new Topic();
        topic.setId(1L);
        topic.setName("Teoria dos Anéis");
        return topic;
    }

    private User createUser() {
        User user = new User();
        user.setId(1L);
        return user;
    }

    private Content createContent(Topic topic) {
        Content content = new Content();
        content.setId(1L);
        content.setTitle("Introdução à Teoria dos Anéis");
        content.setBody("Um anel é uma estrutura algébrica formada por um conjunto com duas operações, " +
                "adição e multiplicação, que satisfazem propriedades específicas. A teoria dos anéis " +
                "estuda essas estruturas e suas relações."
        );
        content.setTopic(topic);
        return content;
    }

    @Test
    void shouldCreateContentWithExistingTopic() {
        Topic topic = createTopic();
        Content content = createContent(topic);

        User user = createUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(topicRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(topic));
        when(contentRepository.save(content)).thenReturn(content);

        Content result = contentService.createContent(1L, content);

        assertSame(content, result);
        assertSame(user, result.getUser());
        assertSame(topic, result.getTopic());

        verify(topicRepository).findByIdAndUserId(1L, 1L);
        verify(contentRepository).save(content);
    }

    @Test
    void shouldThrowBadRequestWhenTopicIdIsNull() {
        Topic topic = new Topic();
        Content content = createContent(topic);

        when(userRepository.findById(1L)).thenReturn(Optional.of(createUser()));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> contentService.createContent(1L, content)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verifyNoInteractions(contentRepository, topicRepository);
    }

    @Test
    void shouldThrowNotFoundWhenCreatingContentWithNonexistentTopic() {
        Content content = createContent(createTopic());

        when(topicRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(createUser()));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> contentService.createContent(1L, content)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(contentRepository, never()).save(any());
    }

    @Test
    void shouldGetAllContents() {
        Content content = createContent(createTopic());

        when(userRepository.findById(1L)).thenReturn(Optional.of(createUser()));
        when(contentRepository.findByUserId(1L)).thenReturn(List.of(content));

        List<Content> result = contentService.getAllContents(1L);

        assertEquals(1, result.size());
        assertSame(content, result.get(0));

        verify(contentRepository).findByUserId(1L);
    }

    @Test
    void shouldGetContentById() {
        Content content = createContent(createTopic());

        when(contentRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(content));

        Content result = contentService.getContentById(1L, 1L);

        assertSame(content, result);
        verify(contentRepository).findByIdAndUserId(1L, 1L);
    }

    @Test
    void shouldThrowNotFoundWhenContentDoesNotExist() {
        when(contentRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> contentService.getContentById(1L, 1L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void shouldUpdateContent() {
        Content existingContent = createContent(createTopic());

        Content updatedContent = new Content();
        updatedContent.setTitle("Qual era a finalidade da Máquina Analítica?");
        updatedContent.setBody("Executar cálculos matemáticos programáveis por meio de instruções, utilizando " +
                "mecanismos de processamento e armazenamento.");

        when(contentRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(existingContent));
        when(contentRepository.save(existingContent)).thenReturn(existingContent);

        Content result = contentService.updateContent(1L, 1L, updatedContent);;

        assertEquals("Qual era a finalidade da Máquina Analítica?", result.getTitle());
        assertEquals("Executar cálculos matemáticos programáveis por meio de instruções, utilizando " +
                "mecanismos de processamento e armazenamento.", result.getBody()
        );

        assertEquals(1L, result.getTopic().getId());

        verify(contentRepository).save(existingContent);
    }

    @Test
    void shouldThrowBadRequestWhenUpdatingWithBlankTitle() {
        Content existingContent = createContent(createTopic());

        Content updatedContent = new Content();
        updatedContent.setTitle("");
        updatedContent.setBody("Conteúdo válido.");

        when(contentRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(existingContent));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> contentService.updateContent(1L, 1L, updatedContent)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(contentRepository, never()).save(any());
    }

    @Test
    void shouldThrowBadRequestWhenUpdatingWithBlankBody() {
        Content existingContent = createContent(createTopic());

        Content updatedContent = new Content();
        updatedContent.setTitle("Teoria dos Anéis");
        updatedContent.setBody("");

        when(contentRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(existingContent));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> contentService.updateContent(1L, 1L, updatedContent)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(contentRepository, never()).save(any());
    }

    @Test
    void shouldThrowNotFoundWhenUpdatingNonexistentContent() {
        Content updatedContent = createContent(createTopic());

        when(contentRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> contentService.updateContent(1L, 1L, updatedContent)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(contentRepository, never()).save(any());
    }

    @Test
    void shouldDeleteExistingContent() {
        Content content = createContent(createTopic());

        when(contentRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(content));

        contentService.deleteContent(1L, 1L);

        verify(contentRepository).delete(content);
    }

    @Test
    void shouldThrowNotFoundWhenDeletingNonexistentContent() {
        when(contentRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> contentService.deleteContent(1L, 1L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(contentRepository, never()).delete(any());
    }

    @Test
    void shouldNotAllowAccessToAnotherUsersContent() {
        when(contentRepository.findByIdAndUserId(1L, 2L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> contentService.getContentById(1L, 2L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void shouldThrowNotFoundWhenCreatingContentWithNonexistentUser() {
        Content content = createContent(createTopic());

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> contentService.createContent(99L, content)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verifyNoInteractions(contentRepository, topicRepository);
    }

    @Test
    void shouldNotCreateContentWithAnotherUsersTopic() {
        Content content = createContent(createTopic());

        when(userRepository.findById(1L)).thenReturn(Optional.of(createUser()));
        when(topicRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> contentService.createContent(1L, content)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(contentRepository, never()).save(any());
    }
}
