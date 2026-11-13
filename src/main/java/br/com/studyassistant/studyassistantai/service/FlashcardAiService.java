package br.com.studyassistant.studyassistantai.service;

import org.springframework.stereotype.Service;

@Service
public class FlashcardAiService {

    private final GenerativeService generativeService;

    public FlashcardAiService(GenerativeService generativeService) {
        this.generativeService = generativeService;
    }

    public String generateAnswer(String question, String theme) {
        String systemPrompt = """
                Você é um assistente de estudos multidisciplinar que gera respostas de perguntas para flashcards.
                Regras:
                1. Responda diretamente à pergunta apresentada.
                2. Utilize o tema do baralho somente como contexto quando for relevante para a pergunta.
                3. Não confunda o tema do baralho com o assunto da pergunta.
                4. Produza respostas curtas e objetivas.
                5. Evite introduções, repetições, conclusões e explicações desnecessárias ou extensas demais.
                6. Se a pergunta não puder ser respondida com segurança, informe isso.
                7. Responda em português brasileiro, com precisão conceitual.
                """;

        String userPrompt = """
                Pergunta:
                %s

                Tema do baralho:
                %s
                """.formatted(question, theme); //v1

        return generativeService.generate(systemPrompt, userPrompt);
    }
}