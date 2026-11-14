package br.com.studyassistant.studyassistantai.service;

import br.com.studyassistant.studyassistantai.dto.GeneratedFlashcard;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.ArrayList;
import java.util.List;

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
                """.formatted(question, theme); // v1

        return generativeService.generate(systemPrompt, userPrompt);
    }

    public List<GeneratedFlashcard> generateFlashcards(
            String theme,
            int quantity,
            String generationContext
    ) {
        String systemPrompt = """
                Você é um assistente de estudos multidisciplinar que gera flashcards.
                Regras:
                1. Gere exatamente a quantidade solicitada de flashcards.
                2. Todos os flashcards devem pertencer ao tema informado.
                3. O tema do baralho define o escopo conceitual principal da geração.
                4. Quando houver contexto adicional, utilize-o para orientar ou delimitar a geração\s
                sem substituir o tema do baralho.
                5. Cada flashcard deve conter uma pergunta clara e uma resposta conceitualmente correta.
                6. As perguntas devem abordar conhecimentos relevantes para estudo e revisão.
                7. As respostas devem ser curtas, objetivas e suficientes para responder à pergunta.
                8. Não repita perguntas.
                9. Responda em português brasileiro.
                10. Não inclua introduções, explicações adicionais ou blocos Markdown.
                11. Retorne cada flashcard exatamente no formato:

                PERGUNTA: <pergunta>
                RESPOSTA: <resposta>
                ---
               \s""";

        String context = generationContext == null || generationContext.isBlank()
                ? "Nenhum contexto adicional fornecido."
                : generationContext;

        String userPrompt = """
                Tema do baralho:
                %s

                Quantidade de flashcards:
                %d

                Contexto adicional:
                %s
                """.formatted(
                theme,
                quantity,
                context
        );

        String generatedContent = generativeService.generate(systemPrompt, userPrompt);

        return parseGeneratedFlashcards(generatedContent, quantity);
    }

    private List<GeneratedFlashcard> parseGeneratedFlashcards(
            String generatedContent,
            int expectedQuantity
    ) {
        String[] blocks = generatedContent.split("---");
        List<GeneratedFlashcard> flashcards = new ArrayList<>();

        for (String block : blocks) {
            String trimmedBlock = block.trim();

            if (trimmedBlock.isBlank()) {
                continue;
            }

            int questionIndex = trimmedBlock.indexOf("PERGUNTA:");
            int answerIndex = trimmedBlock.indexOf("RESPOSTA:");

            if (questionIndex == -1 || answerIndex == -1 || answerIndex <= questionIndex) {
                throw invalidAiResponse();
            }

            String question = trimmedBlock
                    .substring(questionIndex + "PERGUNTA:".length(), answerIndex)
                    .trim();

            String answer = trimmedBlock
                    .substring(answerIndex + "RESPOSTA:".length())
                    .trim();

            if (question.isBlank() || answer.isBlank()) {
                throw invalidAiResponse();
            }

            flashcards.add(new GeneratedFlashcard(question, answer));
        }

        if (flashcards.size() != expectedQuantity) {
            throw invalidAiResponse();
        }

        return flashcards;
    }

    private ResponseStatusException invalidAiResponse() {
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI returned an invalid flashcard format");
    }
}