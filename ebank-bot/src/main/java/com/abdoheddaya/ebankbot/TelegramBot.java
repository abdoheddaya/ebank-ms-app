package com.abdoheddaya.ebankbot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "app.telegram.enabled", havingValue = "true")
public class TelegramBot {
    private static final Logger log = LoggerFactory.getLogger(TelegramBot.class);
    private final ChatService chatService;
    private final RestClient telegram;
    private final long allowedChatId;
    private long offset;

    public TelegramBot(ChatService chatService,
                       @Value("${app.telegram.token}") String token,
                       @Value("${app.telegram.allowed-chat-id}") long allowedChatId) {
        if (token.isBlank() || allowedChatId == 0) {
            throw new IllegalArgumentException("Telegram nécessite un token et TELEGRAM_ALLOWED_CHAT_ID");
        }
        this.chatService = chatService;
        this.telegram = RestClient.create("https://api.telegram.org/bot" + token);
        this.allowedChatId = allowedChatId;
    }

    @Scheduled(fixedDelay = 1000)
    @SuppressWarnings("unchecked")
    public void poll() {
        try {
            Map<String, Object> response = telegram.get()
                    .uri(uri -> uri.path("/getUpdates")
                            .queryParam("offset", offset)
                            .queryParam("timeout", 15)
                            .build())
                    .retrieve().body(Map.class);
            if (response == null || !Boolean.TRUE.equals(response.get("ok"))) return;
            for (Object item : (List<Object>) response.getOrDefault("result", List.of())) {
                Map<String, Object> update = (Map<String, Object>) item;
                offset = ((Number) update.get("update_id")).longValue() + 1;
                Map<String, Object> message = (Map<String, Object>) update.get("message");
                if (message == null || !(message.get("text") instanceof String text)) continue;
                Map<String, Object> chat = (Map<String, Object>) message.get("chat");
                long chatId = ((Number) chat.get("id")).longValue();
                if (chatId != allowedChatId) continue;
                String answer;
                try {
                    answer = chatService.ask(text);
                } catch (Exception e) {
                    log.warn("Échec de la réponse Telegram", e);
                    answer = "Le service est temporairement indisponible.";
                }
                telegram.post().uri("/sendMessage")
                        .body(Map.of("chat_id", chatId, "text", limit(answer, 4096)))
                        .retrieve().toBodilessEntity();
            }
        } catch (Exception e) {
            log.warn("Échec du polling Telegram", e);
        }
    }

    private static String limit(String text, int max) {
        return text == null ? "Aucune réponse" : text.substring(0, Math.min(text.length(), max));
    }
}
