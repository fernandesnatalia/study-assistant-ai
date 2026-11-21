package br.com.studyassistant.studyassistantai.prompt;

import br.com.studyassistant.studyassistantai.dto.RagContext;
import br.com.studyassistant.studyassistantai.enums.PromptPurpose;
import br.com.studyassistant.studyassistantai.model.SourceSelectionMode;
import org.springframework.stereotype.Service;

@Service
public class RagPromptBuilder {

    public SourceSelectionMode resolveMode(SourceSelectionMode mode) {
        return mode == null ? SourceSelectionMode.PREFERRED : mode;
    }

    public String build(
            RagContext ragContext,
            SourceSelectionMode selectionMode,
            PromptPurpose purpose
    ) {
        SourceSelectionMode mode = resolveMode(selectionMode);

        String instructions = switch (purpose) {
            case CONTENT -> contentInstructions(mode);
            case FLASHCARD -> flashcardInstructions(mode);
            case STUDY_ASSISTANT -> assistantInstructions(mode);
        };

        String sources = ragContext.text().isBlank()
                ? "Nenhuma fonte relevante encontrada."
                : ragContext.text();

        return """

                Instruções de fundamentação:
                %s

                Fontes recuperadas:
                %s
                """.formatted(instructions, sources);
    }

    private String contentInstructions(SourceSelectionMode mode) {
        return mode == SourceSelectionMode.EXCLUSIVE
                ? """
                  Utilize exclusivamente as fontes recuperadas para fundamentar o conteúdo.
                  A solicitação e o contexto adicional do usuário servem apenas
                  para orientar o tema e a organização da resposta.
                  Não utilize informações do contexto adicional que não estejam
                  fundamentadas nas fontes recuperadas.
                  Se as fontes forem insuficientes para responder integralmente,
                  explicite essa limitação sem completar com conhecimento externo.
                  """
                : """
                  Priorize as fontes recuperadas quando relevantes.
                  Você pode complementar com conhecimento geral e com o contexto
                  fornecido pelo usuário, respeitando o tópico solicitado.
                  """;
    }

    private String flashcardInstructions(SourceSelectionMode mode) {
        return mode == SourceSelectionMode.EXCLUSIVE
                ? """
                  Utilize exclusivamente as fontes recuperadas para fundamentar
                  as perguntas e respostas dos flashcards.
                  O tema do baralho e o contexto adicional servem apenas
                  para orientar e delimitar a geração.
                  Não utilize informações externas às fontes recuperadas
                  para formular perguntas ou respostas.
                  Se as fontes não sustentarem a quantidade solicitada,
                  não invente informações nem gere perguntas redundantes.
                  """
                : """
                  Priorize as fontes recuperadas quando relevantes.
                  Você pode complementar com conhecimento geral e com
                  o contexto fornecido pelo usuário, respeitando
                  o tema do baralho.
                  """;
    }

    private String assistantInstructions(SourceSelectionMode mode) {
        return mode == SourceSelectionMode.EXCLUSIVE
                ? """
                  Utilize exclusivamente as fontes recuperadas para fundamentar a resposta.
                  A solicitação e o contexto adicional do usuário servem apenas
                  para orientar o assunto e a forma da explicação.
                  Não utilize informações externas às fontes recuperadas.
                  Se as fontes forem insuficientes para responder integralmente,
                  informe a limitação sem complementar com conhecimento externo.
                  """
                : """
                  Priorize as fontes recuperadas quando relevantes.
                  Você pode complementar com conhecimento geral e com o contexto
                  fornecido pelo usuário quando necessário.
                  """;
    }
}