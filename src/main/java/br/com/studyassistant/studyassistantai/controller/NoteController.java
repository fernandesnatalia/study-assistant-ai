package br.com.studyassistant.studyassistantai.controller;

import br.com.studyassistant.studyassistantai.model.Note;
import br.com.studyassistant.studyassistantai.service.AuthenticatedUserService;
import br.com.studyassistant.studyassistantai.service.NoteService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;
    private final AuthenticatedUserService authenticatedUserService;

    public NoteController(
            NoteService noteService,
            AuthenticatedUserService authenticatedUserService
    ) {
        this.noteService = noteService;
        this.authenticatedUserService = authenticatedUserService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Note create(
            @RequestBody Note note,
            Authentication authentication
    ) {
        Long userId = authenticatedUserService.getAuthenticatedUserId(authentication);
        return noteService.create(note, userId);
    }

    @GetMapping
    public List<Note> findAll(Authentication authentication) {
        Long userId = authenticatedUserService.getAuthenticatedUserId(authentication);
        return noteService.findAll(userId);
    }

    @GetMapping("/{id}")
    public Note findById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = authenticatedUserService.getAuthenticatedUserId(authentication);
        return noteService.findById(id, userId);
    }

    @PutMapping("/{id}")
    public Note update(
            @PathVariable Long id,
            @RequestBody Note note,
            Authentication authentication
    ) {
        Long userId = authenticatedUserService.getAuthenticatedUserId(authentication);
        return noteService.update(id, note, userId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = authenticatedUserService.getAuthenticatedUserId(authentication);
        noteService.delete(id, userId);
    }
}