package br.com.studyassistant.studyassistantai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class GenerativeService {

    private final ChatClient chatClient;

    public GenerativeService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String generateAnswer(String question, String content) {
        return chatClient.prompt()
                .system("""
                        Você é um assistente de estudos multidisciplinar que gera respostas de perguntas para flashcards.
                        Regras:
                        1. Responda diretamente à pergunta apresentada.
                        2. Utilize o conteúdo de referência somente quando for relevante para a pergunta.
                        3. Não confunda o assunto do conteúdo com o assunto da pergunta.
                        4. Produza respostas curtas e objetivas.
                        5. Evite introduções, repetições, conclusões e explicações desnecessárias ou extensas demais.
                        6. Se a pergunta não puder ser respondida com segurança, informe isso.
                        7.Responda em português brasileiro, com precisão conceitual.
                        """)
                .user("""
                        Pergunta:
                        %s
                        
                        Conteúdo de referência:
                        %s
                        """.formatted(question,content)) // v1
                .call()
                .content();
    }
}