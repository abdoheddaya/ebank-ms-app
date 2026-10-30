package com.abdoheddaya.ebankbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.CompletableFuture;

@Configuration
public class DiscordBotConfig {
    private static final Logger log = LoggerFactory.getLogger(DiscordBotConfig.class);

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnProperty(name = "app.discord.enabled", havingValue = "true")
    JDA discordBot(ChatService chatService,
                   @Value("${app.discord.token}") String token,
                   @Value("${app.discord.allowed-user-id}") long allowedUserId) {
        if (token.isBlank() || allowedUserId == 0) {
            throw new IllegalArgumentException("Discord nécessite un token et DISCORD_ALLOWED_USER_ID");
        }
        JDA jda = JDABuilder.createDefault(token).addEventListeners(new ListenerAdapter() {
            @Override
            public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
                if (!event.getName().equals("ebank")) return;
                if (event.getUser().getIdLong() != allowedUserId) {
                    event.reply("Accès refusé").setEphemeral(true).queue();
                    return;
                }
                String question = event.getOption("question").getAsString();
                event.deferReply(true).queue(hook -> CompletableFuture.supplyAsync(() -> {
                    try {
                        return chatService.ask(question);
                    } catch (Exception e) {
                        log.warn("Échec de la réponse Discord", e);
                        return "Le service est temporairement indisponible.";
                    }
                }).thenAccept(answer -> hook.editOriginal(
                        answer.substring(0, Math.min(answer.length(), 1900))).queue()));
            }
        }).build();
        jda.updateCommands().addCommands(Commands.slash("ebank", "Interroger l'assistant EBank")
                .addOption(OptionType.STRING, "question", "Votre question", true)).queue();
        return jda;
    }
}
