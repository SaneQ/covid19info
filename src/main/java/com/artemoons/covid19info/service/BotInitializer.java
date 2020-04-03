package com.artemoons.covid19info.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.annotation.PostConstruct;

@Service
@Slf4j
public class BotInitializer extends TelegramLongPollingBot {

    private SiteParser siteParser;

    @Value("${website.url}")
    private String websiteUrl;

    @Value("${telegram.channel-name}")
    private String chatId;

    @Value("${bot.token}")
    private String token;

    @Value("${bot.username}")
    private String username;

    @Autowired
    public BotInitializer(final DefaultBotOptions options, final SiteParser siteParser) {
        super(options);
        this.siteParser = siteParser;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public void onUpdateReceived(Update update) {
        log.info("Method onUpdateReceived fired: {}", update.getMessage().getText());
    }

    @PostConstruct
    public void start() {
        log.info("username: {}, token: {}", username, token);
    }

    @EventListener(ApplicationStartedEvent.class)
    public void onSiteUpdateReceived() {

        String messageText = siteParser.loadSite(websiteUrl);
        SendMessage response = new SendMessage();
        response.setChatId(chatId)
                .setText(messageText)
                .enableMarkdown(true);

        try {
            execute(response);
            log.info("Sent message \"{}\" to {}", messageText, chatId);
        } catch (TelegramApiException ex) {
            log.error("Failed to send message \"{}\" to {} due to error: {}", messageText, chatId, ex.getMessage());
        }
    }
}
