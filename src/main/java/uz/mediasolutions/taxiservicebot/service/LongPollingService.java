package uz.mediasolutions.taxiservicebot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import uz.mediasolutions.taxiservicebot.constants.RestConstants;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class LongPollingService extends TelegramLongPollingBot {

    private final TgService tgService;

    @Override
    public String getBotUsername() {
        return RestConstants.BOT_USERNAME;
    }
    //sakaka_bot

    @Override
    public String getBotToken() {
        return RestConstants.BOT_TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            tgService.getUpdate(update);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
