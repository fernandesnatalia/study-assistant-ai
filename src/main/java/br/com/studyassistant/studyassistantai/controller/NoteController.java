package br.com.studyassistant.studyassistantai.controller;

import br.com.studyassistant.studyassistantai.model.Note;
import br.com.studyassistant.studyassistantai.service.NoteService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Note create(@RequestBody Note note, @RequestParam Long userId) {
        return noteService.create(note, userId);
    }

    @GetMapping
    public List<Note> findAll(@RequestParam Long userId) {
        return noteService.findAll(userId);
    }

    @GetMapping("/{id}")
    public Note findById(@PathVariable Long id, @RequestParam Long userId) {
        return noteService.findById(id, userId);
    }

    @PutMapping("/{id}")
    public Note update(
            @PathVariable Long id,
            @RequestBody Note note,
            @RequestParam Long userId
    ) {
        return noteService.update(id, note, userId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @RequestParam Long userId) {
        noteService.delete(id, userId);
    }
}