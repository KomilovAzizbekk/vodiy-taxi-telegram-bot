package uz.mediasolutions.taxiservicebot.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.Update;
import uz.mediasolutions.taxiservicebot.service.TgService;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/telegram")
public class TgController {

    private final TgService tgService;


    @PostMapping
    public void getPost(@RequestBody Update update) throws IOException {
        tgService.getUpdate(update);
    }

}
