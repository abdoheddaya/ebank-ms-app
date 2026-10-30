package com.abdoheddaya.ebankbot;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Service;

@Service
public class ChatService {
    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder builder, ToolCallbackProvider mcpTools) {
        this.chatClient = builder
                .defaultSystem("Tu es l'assistant de démonstration EBank. Réponds en français. " +
                        "Utilise les outils MCP pour les faits sur les clients et les comptes. " +
                        "Si un service est indisponible, indique-le clairement. " +
                        "Ne prétends jamais avoir modifié des données : tes outils sont en lecture seule.")
                .defaultTools(mcpTools)
                .build();
    }

    public String ask(String message) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Le message ne peut pas être vide");
        }
        return chatClient.prompt().user(message).call().content();
    }
}
