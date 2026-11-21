package br.com.studyassistant.studyassistantai.model;

import br.com.studyassistant.studyassistantai.enums.DeckMode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "decks")
public class Deck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String theme;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @JsonIgnore
    @OneToMany(mappedBy = "deck", cascade = CascadeType.REMOVE)
    private Set<Flashcard> flashcards = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeckMode mode;

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getTheme() { return theme; }

    public void setTheme(String theme) { this.theme = theme; }

    public User getUser() { return user; }

    public void setUser(User user) { this.user = user; }

    public Set<Flashcard> getFlashcards() { return flashcards; }

    public void setFlashcards(Set<Flashcard> flashcards) {
        this.flashcards = flashcards;
    }

    public DeckMode getMode() { return mode; }

    public void setMode(DeckMode mode) { this.mode = mode; }
}