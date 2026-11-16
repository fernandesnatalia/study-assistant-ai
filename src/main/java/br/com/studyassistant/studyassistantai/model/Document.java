package br.com.studyassistant.studyassistantai.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String originalFilename;

    @Column(nullable = false)
    private String format;

    @Column(nullable = false)
    private Long sizeBytes;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    private LocalDateTime processedAt;

    private Integer extractedCharacterCount;

    public Document(User user, String originalFilename, String format, Long sizeBytes) {
        this.user = user;
        this.originalFilename = originalFilename;
        this.format = format;
        this.sizeBytes = sizeBytes;
        this.uploadedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }

    public User getUser() { return user; }

    public String getOriginalFilename() { return originalFilename; }

    public String getFormat() { return format; }

    public Long getSizeBytes() { return sizeBytes; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }

    public LocalDateTime getProcessedAt() { return processedAt; }

    public Integer getExtractedCharacterCount() { return extractedCharacterCount; }

    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }

    public void setExtractedCharacterCount(Integer extractedCharacterCount) {
        this.extractedCharacterCount = extractedCharacterCount;
    }

    protected Document() {
    }
}