package br.com.studyassistant.studyassistantai.dto;

import br.com.studyassistant.studyassistantai.enums.DeckMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class DeckCreateRequest {

    @NotBlank(message = "Theme is required")
    private String theme;

    @NotNull(message = "Mode is required")
    private DeckMode mode;

    @NotBlank(message = "Question is required")
    private String question;

    private String answer;

    public String getTheme() { return theme; }

    public void setTheme(String theme) { this.theme = theme; }

    public DeckMode getMode() { return mode; }

    public void setMode(DeckMode mode) { this.mode = mode; }

    public String getQuestion() { return question; }

    public void setQuestion(String question) { this.question = question; }

    public String getAnswer() { return answer; }

    public void setAnswer(String answer) { this.answer = answer; }
}