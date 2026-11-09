package br.com.studyassistant.studyassistantai.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "answer_attempts")
public class AnswerAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "flashcard_id", nullable = false)
    private Flashcard flashcard;

    @Column(nullable = false)
    private boolean correct;

    @Column(nullable = false, updatable = false)
    private LocalDateTime answeredAt;

    public AnswerAttempt(User user, Flashcard flashcard, boolean correct) {
        this.user = user;
        this.flashcard = flashcard;
        this.correct = correct; this.answeredAt = LocalDateTime.now(); }

    public Long getId() { return id; }

    public User getUser() { return user; }

    public Flashcard getFlashcard() { return flashcard; }

    public boolean isCorrect() { return correct; }

    public LocalDateTime getAnsweredAt() { return answeredAt; }

    protected AnswerAttempt() {
    }
}