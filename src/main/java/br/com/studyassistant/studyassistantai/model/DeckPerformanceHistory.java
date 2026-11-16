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

@Entity
@Table(name = "deck_performance_histories")
public class DeckPerformanceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Long originalDeckId;

    @Column(nullable = false)
    private String theme;

    @Column(nullable = false)
    private long totalAttempts;

    @Column(nullable = false)
    private long correctAnswers;

    @Column(nullable = false)
    private long incorrectAnswers;

    @Column(nullable = false)
    private double accuracyPercentage;

    public DeckPerformanceHistory(
            User user,
            Long originalDeckId,
            String theme,
            long totalAttempts,
            long correctAnswers,
            long incorrectAnswers,
            double accuracyPercentage
    ) {
        this.user = user;
        this.originalDeckId = originalDeckId;
        this.theme = theme;
        this.totalAttempts = totalAttempts;
        this.correctAnswers = correctAnswers;
        this.incorrectAnswers = incorrectAnswers;
        this.accuracyPercentage = accuracyPercentage;
    }

    public Long getId() { return id; }

    public User getUser() { return user; }

    public Long getOriginalDeckId() { return originalDeckId; }

    public String getTheme() { return theme; }

    public long getTotalAttempts() { return totalAttempts; }

    public long getCorrectAnswers() { return correctAnswers; }

    public long getIncorrectAnswers() { return incorrectAnswers; }

    public double getAccuracyPercentage() { return accuracyPercentage; }

    protected DeckPerformanceHistory() {
    }
}
