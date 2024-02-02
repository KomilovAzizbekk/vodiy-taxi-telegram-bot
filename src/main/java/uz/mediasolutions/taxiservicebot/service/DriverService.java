package uz.mediasolutions.taxiservicebot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import uz.mediasolutions.taxiservicebot.constants.RestConstants;
import uz.mediasolutions.taxiservicebot.entity.Car;
import uz.mediasolutions.taxiservicebot.entity.Driver;
import uz.mediasolutions.taxiservicebot.payload.BotState;
import uz.mediasolutions.taxiservicebot.repository.*;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class DriverService {

    private final DriverRepository driverRepository;

    private final RestTemplate restTemplate;

    private final CarRepository carRepository;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


    private final Map<Long, BotState> userStates = new HashMap<>();

    public void setUserState(Long chatId, BotState state) {
        userStates.put(chatId, state);
    }

    public BotState getUserState(Long chatId) {
        return userStates.getOrDefault(chatId, BotState.START);
    }

//    private int messageCounter = 0;

//    public void sendMessageToDrivers() {
//        List<Driver> drivers = driverRepository.findAll();
//        for (Driver driver : drivers) {
//            if (driver.getPaidTime() != null) {
//                LocalDateTime paymentDeadline = LocalDateTime.from(driver.getPaidTime()).plusMonths(1L);
//                LocalDateTime now = LocalDateTime.now();
//                LocalDateTime time = LocalDateTime.from(paymentDeadline).minusDays(3);
//                if (now.isAfter(paymentDeadline)) {
//                    driver.setActive(false);
//                    driverRepository.save(driver);
//                    SendMessage sendMessage = new SendMessage(driver.getChatId(),
//                            "❗\uFE0FHurmatli haydovchi, sizning 1 oylik obunangiz yakunlandi");
//                    restTemplate.postForObject(RestConstants.FOR_MESSAGE, sendMessage, Object.class);
//                } else if (now.isAfter(time)) {
//                    scheduler.scheduleAtFixedRate(() -> {
//                        if (messageCounter < 4) {
//                            try {
//                                SendMessage sendMessage = new SendMessage(driver.getChatId(),
//                                        "❗\uFE0FHurmatli haydovchi, obunangiz yakunlanish sanasi: " +
//                                                paymentDeadline.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
//                                messageCounter++;
//                                restTemplate.postForObject(RestConstants.FOR_MESSAGE, sendMessage, Object.class);
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        } else {
//                            scheduler.shutdown(); // Stop scheduling after four messages
//                        }
//                    }, 0, 12, TimeUnit.HOURS);
//                }
//            }
//
//        }
//    }


    //GET CHAT ID
    public static String getChatId(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getChatId().toString();
        } else if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getFrom().getId().toString();
        }
        return "";
    }


    public static Long longChatId(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getChatId();
        } else if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getMessage().getChatId();
        }
        return null;
    }


    public void saveDriver(Update update) {
        String chatId = getChatId(update);
        try {
            if (driverRepository.existsByChatId(chatId)) {
                Driver driver = driverRepository.findByChatId(chatId);
                if (driver.getCar() != null && driver.getFIO() != null &&
                        driver.getPhoneNumber2() != null && driver.getPhoneNumber1() != null) {
                    setUserState(longChatId(update), BotState.BACK_OR_CONFIRM_PAYMENT);
                    me(update);
                } else {
                    setUserState(longChatId(update), BotState.DRIVER_FIO);
                    driverFio(update);
                }
            } else {
                Driver driver = Driver.builder().chatId(chatId).build();
                driverRepository.save(driver);
                setUserState(longChatId(update), BotState.DRIVER_FIO);
                driverFio(update);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void me(Update update) {
        String chatId = getChatId(update);
        Driver driver = driverRepository.findByChatId(chatId);

        String text = "*\uD83D\uDC47\uD83D\uDC47\uD83D\uDC47Sizning haydovchi sifatida ma'lumotlaringiz\uD83D\uDC47\uD83D\uDC47\uD83D\uDC47 \n*" +
                "*\uD83E\uDD35\uD83C\uDFFD\u200D♂\uFE0FFIO: *" + driver.getFIO() + "\n" +
                "*\uD83D\uDCF11-Telefon raqam:  *" + driver.getPhoneNumber1() + "\n" +
                "*\uD83D\uDCF12-Telefon raqam:  *" + driver.getPhoneNumber2() + "\n" +
                "*\uD83D\uDE96Avtomobil markasi:  *" + driver.getCar().getModel() + "\n" +
                "*\uD83D\uDE96Avtomobil raqami:  *" + driver.getCar().getNumber() + "\n";

        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        editMessageText.setReplyMarkup(forMe1());
        editMessageText.enableMarkdown(true);
        editMessageText.setText(text);

        try {
            restTemplate.postForObject(RestConstants.EDIT_MESSAGE, editMessageText, Object.class);
            setUserState(longChatId(update), BotState.BACK_OR_CONFIRM_PAYMENT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private InlineKeyboardMarkup forMe1() {

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        InlineKeyboardButton button1 = new InlineKeyboardButton();
        InlineKeyboardButton button2 = new InlineKeyboardButton();

        button1.setText("♻\uFE0FQayta ro'yxatdan o'tish");
        button2.setText("\uD83D\uDD19Ortga");

        button1.setCallbackData("driverReRegister");
        button2.setCallbackData("OrtgaDriver");

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();

        row1.add(button1);
        row2.add(button2);

        rowsInline.add(row1);
        rowsInline.add(row2);

        markupInline.setKeyboard(rowsInline);

        return markupInline;
    }


    public void driverReRegister(Update update) {
        setUserState(longChatId(update), BotState.DRIVER_FIO);
        driverFio(update);
    }


    //TODO TO'LOV QO'SHILHANDAN KEYIN ISHLAYDI
//    private InlineKeyboardMarkup forMe2() {
//
//        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
//        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
//
//
//        InlineKeyboardButton button1 = new InlineKeyboardButton();
//        InlineKeyboardButton button2 = new InlineKeyboardButton();
//
//        button1.setText("✅To'lovni tasdiqlash✅");
//        button2.setText("\uD83D\uDD19Ortga");
//
//        button1.setCallbackData("Tolovni tasdiqlash");
//        button2.setCallbackData("OrtgaDriver");
//
//        List<InlineKeyboardButton> row1 = new ArrayList<>();
//        List<InlineKeyboardButton> row2 = new ArrayList<>();
//        row1.add(button1);
//        row2.add(button2);
//
//        rowsInline.add(row1);
//        rowsInline.add(row2);
//
//        markupInline.setKeyboard(rowsInline);
//
//        return markupInline;
//    }


    //INLINE QUERYDA HAYDOVCHI TUGMASINI BOSGANDA ISHLAYDI
    public void driverFio(Update update) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setText("*Haydovchi rolini tanladingiz.*\n" +
                "\uD83E\uDD35\uD83C\uDFFD\u200D♂\uFE0FFIO (Familiya Ism Otchestvo)ni kiriting:");
        editMessageText.setChatId(getChatId(update));
        editMessageText.enableMarkdown(true);
        editMessageText.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        try {
            restTemplate.postForObject(RestConstants.EDIT_MESSAGE, editMessageText, Object.class);
            setUserState(longChatId(update), BotState.DRIVER_PHONE_1);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    //HAYDOVCHI FIO NI KIRITGANDAN SO'NG 1-TELEFON RAQAMINI SO'RAYDI
    public void driverPhone1(Update update) {
        Driver driver = driverRepository.findByChatId(getChatId(update));
        driver.setFIO(update.getMessage().getText());
        driverRepository.save(driver);
        SendMessage sendMessage = new SendMessage(getChatId(update),
                "\uD83D\uDCF11-Telefon raqamingizni kiriting:\n" +
                        "Namuna: +998XXYYYYYYY");
        sendMessage.setReplyMarkup(generateMarkup());
        try {
            restTemplate.postForObject(RestConstants.FOR_MESSAGE, sendMessage, Object.class);
            setUserState(longChatId(update), BotState.DRIVER_PHONE_2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static ReplyKeyboardMarkup generateMarkup() {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        List<KeyboardRow> rowList = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        KeyboardButton button1 = new KeyboardButton();
        button1.setText("\uD83D\uDCF1Mening raqamim");
        button1.setRequestContact(true);
        row1.add(button1);
        rowList.add(row1);
        markup.setKeyboard(rowList);
        markup.setSelective(true);
        markup.setResizeKeyboard(true);
        return markup;
    }


    //HAYDOOVCHI 1-TELEFON RAQAMINI KIRITGANDAN SO'NG 2-TELEFON RAQAMINI SO'RAYDI
    public void driverPhone2(Update update) {
        Driver driver = driverRepository.findByChatId(getChatId(update));
        if (update.getMessage().hasText()) {
            if (isValidPhoneNumber(update.getMessage().getText())) {
                String phoneNumber = update.getMessage().getText();
                driver.setPhoneNumber1(phoneNumber);
                executeDriverPhone2(update, driver);
            } else {
                SendMessage sendMessage = new SendMessage(getChatId(update),
                        "Telefon raqam formati xato. Qayta kiriting:");
                sendMessage.setReplyMarkup(generateMarkup());
                restTemplate.postForObject(RestConstants.FOR_MESSAGE, sendMessage, Object.class);
                setUserState(longChatId(update), BotState.INCORRECT_DRIVER_PHONE_1);
            }
        } else if (update.getMessage().hasContact()) {
            String phoneNumber = update.getMessage().getContact().getPhoneNumber();
            phoneNumber = phoneNumber.startsWith("+") ? phoneNumber : "+" + phoneNumber;
            driver.setPhoneNumber1(phoneNumber);
            executeDriverPhone2(update, driver);
        }
    }


    private void executeDriverPhone2(Update update, Driver driver) {
        driverRepository.save(driver);
        SendMessage sendMessage = new SendMessage(getChatId(update),
                "\uD83D\uDCF12-Telefon raqamingizni kiriting:\n" +
                        "Namuna: +998XXYYYYYYY");
        sendMessage.setReplyMarkup(generateMarkup());
        try {
            restTemplate.postForObject(RestConstants.FOR_MESSAGE, sendMessage, Object.class);
            setUserState(longChatId(update), BotState.CAR_MODEL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //HAYDOOVCHI 2-TELEFON RAQAMINI KIRITGANDAN SO'NG MOSHINA MARKASINI SO'RAYDI
    public void carModel(Update update) {
        Driver driver = driverRepository.findByChatId(getChatId(update));
        if (update.getMessage().hasText()) {
            if (isValidPhoneNumber(update.getMessage().getText())) {
                String phoneNumber = update.getMessage().getText();
                driver.setPhoneNumber2(phoneNumber);
                executeCarModel(update, driver);
            } else {
                SendMessage sendMessage = new SendMessage(getChatId(update),
                        "Telefon raqam formati xato. Qayta kiriting:");
                sendMessage.setReplyMarkup(generateMarkup());
                restTemplate.postForObject(RestConstants.FOR_MESSAGE, sendMessage, Object.class);
                setUserState(longChatId(update), BotState.INCORRECT_DRIVER_PHONE_2);
            }
        } else if (update.getMessage().hasContact()) {
            String phoneNumber = update.getMessage().getContact().getPhoneNumber();
            phoneNumber = phoneNumber.startsWith("+") ? phoneNumber : "+" + phoneNumber;
            driver.setPhoneNumber2(phoneNumber);
            executeCarModel(update, driver);
        }
    }


    private void executeCarModel(Update update, Driver driver) {
        driverRepository.save(driver);
        SendMessage sendMessage = new SendMessage(getChatId(update),
                "\uD83D\uDE96Moshinangizni markasini kiriting:");
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
        try {
            restTemplate.postForObject(RestConstants.FOR_MESSAGE, sendMessage, Object.class);
            setUserState(longChatId(update), BotState.CAR_NUMBER);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void incorrectDriverPhone1(Update update) {
        driverPhone2(update);
    }

    public void incorrectDriverPhone2(Update update) {
        carModel(update);
    }


    //HAYDOOVCHI MOSHINA MARKASINI KIRITGANDAN SO'NG MOSHINA RAQAMINI SO'RAYDI
    public void carNumber(Update update) {
        Driver driver = driverRepository.findByChatId(getChatId(update));
        Car car = Car.builder().model(update.getMessage().getText()).build();
        Car savedCar = carRepository.save(car);
        driver.setCar(savedCar);
        driverRepository.save(driver);
        SendMessage sendMessage = new SendMessage(getChatId(update),
                "\uD83D\uDE96Moshinangizni raqamini kiriting:");
        try {
            restTemplate.postForObject(RestConstants.FOR_MESSAGE, sendMessage, Object.class);
            setUserState(longChatId(update), BotState.DRIVER_REGISTRATION_FINISHED);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void registrationFinished(Update update) {
        Car car = carRepository.findByUserChatId(getChatId(update));
        car.setNumber(update.getMessage().getText());
        Car savedCar = carRepository.save(car);
        Driver driver = driverRepository.findByChatId(getChatId(update));
        driver.setCar(savedCar);
        driverRepository.save(driver);
        SendMessage sendMessage = new SendMessage(getChatId(update),
                "*Siz muvaffaqiyatli ro'yxatdan o'tdingiz✅.*\n" +
                        "Haydovchilar umumiy kanaliga qo'shilishingiz" +
                        "mumkin. U yerga barcha yo'lovchilarning buyurtmalari kelib tushadi.");
        sendMessage.setReplyMarkup(forRegistrationFinished());
        sendMessage.enableMarkdown(true);
        try {
            restTemplate.postForObject(RestConstants.FOR_MESSAGE, sendMessage, Object.class);
            setUserState(longChatId(update), BotState.MENU_OR_CONFIRM_PAYMENT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private InlineKeyboardMarkup forRegistrationFinished() {

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        InlineKeyboardButton button1 = new InlineKeyboardButton();

        button1.setText("Kanalga qo'shilish");

        button1.setUrl("https://t.me/+htraJW4r4aZkZjNi");

        List<InlineKeyboardButton> row1 = new ArrayList<>();

        row1.add(button1);

        rowsInline.add(row1);

        markupInline.setKeyboard(rowsInline);

        return markupInline;
    }


    private static boolean isValidPhoneNumber(String phoneNumber) {
        String regex = "\\+998[1-9]\\d{8}";

        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(phoneNumber);

        return matcher.matches();
    }


//TO'LOV QO'SHILSA ISHLATILADIGAN QISM

    //    public void sendPayment(Update update) {
//
//        SendMessage sendMessage = new SendMessage(getChatId(update),
//                "9860600437865527 karta raqamiga *25000* so'm miqdorida obuna to'lovini amalga oshiring" +
//                        " va ushbu matn tagida to'lov screenshotini (isbot) yuboring\n" +
//                        "❗\uFE0FFaqat *1 dona* rasm jo'nating.");
//        sendMessage.enableMarkdown(true);
//        try {
//            restTemplate.postForObject(RestConstants.FOR_MESSAGE, sendMessage, Object.class);
//            setUserState(longChatId(update), BotState.SEND_SCREENSHOT);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    public void sendPhotoToChannel(Update update) {
//        if ((!update.hasMessage() || (update.hasMessage() && !update.getMessage().hasPhoto())) ||
//                (update.getMessage().getPhoto().size() > 4)) {
//            SendMessage sendMessage = new SendMessage(getChatId(update),
//                    "❗\uFE0FHurmatli haydovchi siz bu yerda faqat *1 tadan ko'p bo'lmagan rasm* jo'nata olasiz.\n" +
//                            "Qaytadan urinib ko'ring");
//            sendMessage.enableMarkdown(true);
//            try {
//                restTemplate.postForObject(RestConstants.FOR_MESSAGE, sendMessage, Object.class);
//                setUserState(longChatId(update), BotState.INCORRECT_PHOTO_SIZE);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        } else {
//
//            String chatId = getChatId(update);
//            Driver driver = driverRepository.findByChatId(chatId);
//            String text = "*\uD83E\uDD35\uD83C\uDFFD\u200D♂\uFE0FFIO:  *" + driver.getFIO() + "\n" +
//                    "*\uD83D\uDCF11-Telefon raqam:  *" + driver.getPhoneNumber1() + "\n" +
//                    "*\uD83D\uDCF12-Telefon raqam:  *" + driver.getPhoneNumber2() + "\n" +
//                    "*\uD83D\uDE96Avtomobil markasi:  *" + driver.getCar().getModel() + "\n" +
//                    "*\uD83D\uDE96Avtomobil raqami:  *" + driver.getCar().getNumber() + "\n" +
//                    "*⏳Status:  *" + (driver.isActive() ? "Faol (To'lov qilingan)" : "Passiv (To'lov qilinmagan)") + "\n" +
//                    (driver.getPaidTime() != null ? "*\uD83D\uDCB0To'lov amalga oshirilgan:  *" +
//                            driver.getPaidTime().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) : "");
//
//
//            ForwardMessage forwardMessage = new ForwardMessage();
//            forwardMessage.setChatId("-1002114083061");
//            forwardMessage.setFromChatId(chatId);
//            forwardMessage.setMessageId(update.getMessage().getMessageId());
//
//
//            SendMessage sendMessage = new SendMessage("-1002114083061", text);
//            sendMessage.setReplyMarkup(forSendPhoto(chatId));
//            sendMessage.enableMarkdown(true);
//
//
//            SendMessage sendMessage1 = new SendMessage(getChatId(update),
//                    "*To'lovingiz uchun raxmat\uD83E\uDD1D\n*" +
//                            "*❗\uFE0FAdmin to'lovingizni tasdiqlashi bilan haydovchilar guruhiga qo'shilish " +
//                            "imkoniga ega bo'lasiz.*");
//            sendMessage1.enableMarkdown(true);
//            sendMessage1.setReplyMarkup(forMenu());
//            try {
//                restTemplate.postForObject(RestConstants.FORWARD, forwardMessage, Object.class);
//                restTemplate.postForObject(RestConstants.FOR_MESSAGE, sendMessage1, Object.class);
//                restTemplate.postForObject(RestConstants.FOR_MESSAGE, sendMessage, Object.class);
//                setUserState(longChatId(update), BotState.DRIVER_MENU);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//
//    private InlineKeyboardMarkup forSendPhoto(String chatId) {
//
//        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
//        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
//
//
//        InlineKeyboardButton button1 = new InlineKeyboardButton();
//        InlineKeyboardButton button2 = new InlineKeyboardButton();
//
//        button1.setText("✅Qabul qilish✅");
//        button2.setText("❌Rad etish❌");
//
//        button1.setCallbackData("accept" + chatId);
//        button2.setCallbackData("reject" + chatId);
//
//        List<InlineKeyboardButton> row1 = new ArrayList<>();
//        List<InlineKeyboardButton> row2 = new ArrayList<>();
//        row1.add(button1);
//        row2.add(button2);
//
//        rowsInline.add(row1);
//        rowsInline.add(row2);
//
//        markupInline.setKeyboard(rowsInline);
//
//        return markupInline;
//    }
//
//
//    public void acceptScreenshot(Update update, String driverChatId) {
//        Driver driver = driverRepository.findByChatId(driverChatId);
//        System.out.println(driverChatId);
//        driver.setActive(true);
//        driver.setPaidTime(LocalDateTime.now());
//        driverRepository.save(driver);
//        SendMessage sendMessage = new SendMessage(driverChatId,
//                "Sizning to'lovingiz admin tomonidan tasdiqlandi.✅\n" +
//                        "Haydovchilar guruhiga qo'shilishingiz mumkin.\n" +
//                        "https://t.me/+Ba-14ks_dTxhMzZi ");
//        sendMessage.setReplyMarkup(forMenu());
//        deleteMessageWhenAccept(update, driverChatId);
//        try {
//            restTemplate.postForObject(RestConstants.FOR_MESSAGE, sendMessage, Object.class);
//            setUserState(Long.valueOf(driverChatId), BotState.DRIVER_MENU);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }
//
//
//    public void rejectScreenshot(Update update, String driverChatId) {
//        SendMessage sendMessage = new SendMessage(driverChatId,
//                "Sizning to'lovingiz admin tomonidan rad etildi❌.\n" +
//                        "Iltimos to'lov cheki haqiqiy ekanligiga ishonch hosil qilib so'ng jo'nating.");
//        sendMessage.setReplyMarkup(forMenu());
//        deleteMessageWhenReject(update, driverChatId);
//        try {
//            restTemplate.postForObject(RestConstants.FOR_MESSAGE, sendMessage, Object.class);
//            setUserState(Long.valueOf(driverChatId), BotState.DRIVER_MENU);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private InlineKeyboardMarkup forMenu() {
//
//        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
//        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
//
//
//        InlineKeyboardButton button1 = new InlineKeyboardButton();
//
//        button1.setText("\uD83D\uDCCCBosh Menyu\uD83D\uDCCC");
//
//        button1.setCallbackData("Menu");
//
//        List<InlineKeyboardButton> row1 = new ArrayList<>();
//
//        row1.add(button1);
//
//        rowsInline.add(row1);
//
//        markupInline.setKeyboard(rowsInline);
//
//        return markupInline;
//    }
//
//
//    private void deleteMessageWhenAccept(Update update, String driverChatId) {
//        DeleteMessage deleteMessage = new DeleteMessage();
//        deleteMessage.setChatId("-1002114083061");
//        deleteMessage.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
//
//        Driver driver = driverRepository.findByChatId(driverChatId);
//        String text = "*\uD83E\uDD35\uD83C\uDFFD\u200D♂\uFE0FFIO:  *" + driver.getFIO() + "\n" +
//                "*\uD83D\uDCF11-Telefon raqam:  *" + driver.getPhoneNumber1() + "\n" +
//                "*\uD83D\uDCF12-Telefon raqam:  *" + driver.getPhoneNumber2() + "\n" +
//                "*\uD83D\uDE96Avtomobil markasi:  *" + driver.getCar().getModel() + "\n" +
//                "*\uD83D\uDE96Avtomobil raqami:  *" + driver.getCar().getNumber() + "\n" +
//                "*⏳Status:  *" + (driver.isActive() ? "Faol (To'lov qilingan)" : "Passiv (To'lov qilinmagan)") + "\n" +
//                (driver.getPaidTime() != null ? "*\uD83D\uDCB0To'lov amalga oshirilgan:  *" +
//                        driver.getPaidTime().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) : "") + "\n" +
//                "*✅Qabul qilindi✅*";
//
//        SendMessage sendMessage = new SendMessage("-1002114083061", text);
//        sendMessage.enableMarkdown(true);
//
//        try {
//            restTemplate.postForObject(RestConstants.FOR_DELETE_MESSAGE, deleteMessage, Object.class);
//            restTemplate.postForObject(RestConstants.FOR_MESSAGE, sendMessage, Object.class);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    private void deleteMessageWhenReject(Update update, String driverChatId) {
//        DeleteMessage deleteMessage = new DeleteMessage();
//        deleteMessage.setChatId("-1002114083061");
//        deleteMessage.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
//
//        Driver driver = driverRepository.findByChatId(driverChatId);
//        String text = "*\uD83E\uDD35\uD83C\uDFFD\u200D♂\uFE0FFIO:  *" + driver.getFIO() + "\n" +
//                "*\uD83D\uDCF11-Telefon raqam:  *" + driver.getPhoneNumber1() + "\n" +
//                "*\uD83D\uDCF12-Telefon raqam:  *" + driver.getPhoneNumber2() + "\n" +
//                "*\uD83D\uDE96Avtomobil markasi:  *" + driver.getCar().getModel() + "\n" +
//                "*\uD83D\uDE96Avtomobil raqami:  *" + driver.getCar().getNumber() + "\n" +
//                "*⏳Status:  *" + (driver.isActive() ? "Faol (To'lov qilingan)" : "Passiv (To'lov qilinmagan)") + "\n" +
//                (driver.getPaidTime() != null ? "*\uD83D\uDCB0To'lov amalga oshirilgan:  *" +
//                        driver.getPaidTime().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) : "") + "\n" +
//                "*❌Rad etildi❌*";
//
//        SendMessage sendMessage = new SendMessage("-1002114083061", text);
//        sendMessage.enableMarkdown(true);
//
//        try {
//            restTemplate.postForObject(RestConstants.FOR_DELETE_MESSAGE, deleteMessage, Object.class);
//            restTemplate.postForObject(RestConstants.FOR_MESSAGE, sendMessage, Object.class);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void incorrectPhotoSize(Update update) {
//        sendPhotoToChannel(update);
//    }
//
//    public void notPhoto(Update update) {
//        SendMessage sendMessage = new SendMessage(getChatId(update),
//                "❗\uFE0FHurmatli haydovchi siz bu yerda faqat *1 tadan ko'p bo'lmagan rasm* jo'nata olasiz.\n" +
//                        "Qaytadan urinib ko'ring");
//        sendMessage.enableMarkdown(true);
//        try {
//            restTemplate.postForObject(RestConstants.FOR_MESSAGE, sendMessage, Object.class);
//            setUserState(longChatId(update), BotState.INCORRECT_PHOTO_SIZE);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }



}
