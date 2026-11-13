package br.com.studyassistant.studyassistantai.dto;

import jakarta.validation.constraints.NotBlank;

public class DeckCreateRequest {

    @NotBlank(message = "Theme is required")
    private String theme;

    @NotBlank(message = "Question is required")
    private String question;

    private String answer;

    public String getTheme() { return theme; }

    public void setTheme(String theme) { this.theme = theme; }

    public String getQuestion() { return question; }

    public void setQuestion(String question) { this.question = question; }

    public String getAnswer() { return answer; }

    public void setAnswer(String answer) { this.answer = answer; }
}