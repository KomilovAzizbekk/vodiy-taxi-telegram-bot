package uz.mediasolutions.taxiservicebot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import uz.mediasolutions.taxiservicebot.constants.RestConstants;
import uz.mediasolutions.taxiservicebot.entity.TourInfo;
import uz.mediasolutions.taxiservicebot.entity.User;
import uz.mediasolutions.taxiservicebot.payload.BotState;
import uz.mediasolutions.taxiservicebot.repository.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final RestTemplate restTemplate;

    private final DistrictRepository districtRepository;

    private final RegionRepository regionRepository;

    private final TourInfoRepository tourInfoRepository;


    private final Map<Long, BotState> userStates = new HashMap<>();

    public void setUserState(Long chatId, BotState state) {
        userStates.put(chatId, state);
    }

    public BotState getUserState(Long chatId) {
        return userStates.getOrDefault(chatId, BotState.START);
    }


    //GET CHAT ID  method*********************************************************
    public static String getChatId(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getChatId().toString();
        } else if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getMessage().getChatId().toString();
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


    //RO'YXATDAN O'TAYOTGAN VAQTDA HAYDOVCHI TUGMASI BOSILGANDA ISHLAYDI
    public void saveUser(Update update) {
        String chatId = getChatId(update);
        try {
            if (userRepository.existsByChatId(chatId)) {
                User user = userRepository.findByChatId(chatId);
                if (user.getFirstName() != null && user.getPhoneNumber() != null) {
                    setUserState(longChatId(update), BotState.BACK_OR_TAXI_BOOK);
                    me(update);
                } else {
                    setUserState(longChatId(update), BotState.USER_FIO);
                    userFio(update);
                }
            } else {
                User user = User.builder().chatId(chatId).build();
                userRepository.save(user);
                setUserState(longChatId(update), BotState.USER_FIO);
                userFio(update);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //AGAR USHBU FOYDALANUVCHI ALLAQACHON YO'LOVCHI SIFATIDA RO'YXATDAN O'TGAN BO'LSA ISHLAYDI
    private void me(Update update) {
        String chatId = getChatId(update);
        User user = userRepository.findByChatId(chatId);
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(getChatId(update));
        editMessageText.enableMarkdown(true);
        editMessageText.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        editMessageText.setText("*\uD83D\uDC47\uD83D\uDC47\uD83D\uDC47Sizning yo'lovchi sifatida ma'lumotlaringiz\uD83D\uDC47\uD83D\uDC47\uD83D\uDC47 \n\n*" +
                "*\uD83D\uDC6BIsm:  *" + user.getFirstName() + "\n" +
                "*\uD83D\uDCF1Telefon raqam:  *" + user.getPhoneNumber());
        editMessageText.setReplyMarkup(forMe());
        try {
            restTemplate.postForObject(RestConstants.EDIT_MESSAGE, editMessageText, Object.class);
            setUserState(longChatId(update), BotState.BACK_OR_TAXI_BOOK);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private InlineKeyboardMarkup forMe() {

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();


        InlineKeyboardButton button1 = new InlineKeyboardButton();
        InlineKeyboardButton button2 = new InlineKeyboardButton();
        InlineKeyboardButton button3 = new InlineKeyboardButton();

        button1.setText("\uD83D\uDE96Taxi buyurtma berish\uD83D\uDE96");
        button2.setText("♻\uFE0FQayta ro'yxatdan o'tish");
        button3.setText("\uD83D\uDD19Ortga");

        button1.setCallbackData("taxiBuyurtma");
        button2.setCallbackData("userReRegister");
        button3.setCallbackData("OrtgaUser");

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        List<InlineKeyboardButton> row3 = new ArrayList<>();

        row1.add(button1);
        row2.add(button2);
        row3.add(button3);

        rowsInline.add(row1);
        rowsInline.add(row2);
        rowsInline.add(row3);

        markupInline.setKeyboard(rowsInline);

        return markupInline;
    }


    public void userReRegister(Update update) {
        setUserState(longChatId(update), BotState.USER_FIO);
        userFio(update);
    }


    //HAYDOVCHI TUGMASI BOSILGANDA USER DATABASEDA MAVJUD BO'MASA ISHLAYDI
    public void userFio(Update update) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(getChatId(update));
        editMessageText.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        editMessageText.setText("*Yo'lovchi rolini tanladingiz.*\n" +
                "\uD83E\uDD35\uD83C\uDFFD\u200D♂\uFE0FIsmingizni kiriting:");
        editMessageText.enableMarkdown(true);
        try {
            restTemplate.postForObject(RestConstants.EDIT_MESSAGE, editMessageText, Object.class);
            setUserState(longChatId(update), BotState.PASSENGER_PHONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //YO'LOVCHI ISMINI KIRITGANDAN SO'NG ISHLAYDI
    public void userPhone(Update update) {
        User user = userRepository.findByChatId(getChatId(update));
        user.setFirstName(update.getMessage().getText());

        userRepository.save(user);
        SendMessage sendMessage = new SendMessage(update.getMessage().getChatId().toString(),
                "\uD83D\uDCF1Telefon raqamingizni kiriting:\n" +
                        "Namuna: +998XXYYYYYYY\n" +
                        "❗\uFE0FHaydovchilarimiz sizga qo'ng'iroq qilishlari uchun hozirda " +
                        "faol bo'lgan raqamdan foydalanishingizni so'raymiz.");
        sendMessage.setReplyMarkup(generateMarkup());
        try {
            restTemplate.postForObject(RestConstants.FOR_MESSAGE, sendMessage, Object.class);
            setUserState(longChatId(update), BotState.PASSENGER_REGISTRATION_FINISHED);
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


    public void incorrectUserPhone(Update update) {
        registerDone(update);
    }


    //YO'LOVCHI TELEFON RAQAMINI KIRITGANDAN SO'NG ISHLAYDI. REGISTRATSIYA SHU JOYIDA TUGAYDI
    public void registerDone(Update update) {
        User user = userRepository.findByChatId(getChatId(update));
        if (update.getMessage().hasText()) {
            if (isValidPhoneNumber(update.getMessage().getText())) {
                String phoneNumber = update.getMessage().getText();
                user.setPhoneNumber(phoneNumber);
                executeRegisterDone(update, user);
            } else {
                SendMessage sendMessage = new SendMessage(getChatId(update),
                        "Telefon raqam formati xato. Qayta kiriting:");
                sendMessage.setReplyMarkup(generateMarkup());
                restTemplate.postForObject(RestConstants.FOR_MESSAGE, sendMessage, Object.class);
                setUserState(longChatId(update), BotState.INCORRECT_USER_PHONE);
            }
        } else if (update.getMessage().hasContact()) {
            String phoneNumber = update.getMessage().getContact().getPhoneNumber();
            phoneNumber = phoneNumber.startsWith("+") ? phoneNumber : "+" + phoneNumber;
            user.setPhoneNumber(phoneNumber);
            executeRegisterDone(update, user);
        }
    }

    private void executeRegisterDone(Update update, User user) {
        userRepository.save(user);
        SendMessage sendMessage1 = new SendMessage(getChatId(update),
                "*Xurmatli yo'lovchi, siz muvaffaqiyatli ro'yxatdan o'tdingiz✅*");
        SendMessage sendMessage2 = new SendMessage(getChatId(update),
                        "*\uD83D\uDC47\uD83D\uDC47\uD83D\uDC47Sizning ma'lumotlaringiz\uD83D\uDC47\uD83D\uDC47\uD83D\uDC47 \n\n*" +
                        "*\uD83D\uDC6BIsm:  *" + user.getFirstName() + "\n" +
                        "*\uD83D\uDCF1Telefon raqam:   *" + user.getPhoneNumber());
        sendMessage1.enableMarkdown(true);
        sendMessage1.setReplyMarkup(new ReplyKeyboardRemove(true));
        sendMessage2.enableMarkdown(true);
        sendMessage2.setReplyMarkup(forRegisterDone());
        try {
            restTemplate.postForObject(RestConstants.FOR_MESSAGE, sendMessage1, Object.class);
            restTemplate.postForObject(RestConstants.FOR_MESSAGE, sendMessage2, Object.class);
            setUserState(longChatId(update), BotState.MENU_OR_TAXI_BOOK);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private InlineKeyboardMarkup forRegisterDone() {

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();


        InlineKeyboardButton button1 = new InlineKeyboardButton();
        InlineKeyboardButton button2 = new InlineKeyboardButton();
        InlineKeyboardButton button3 = new InlineKeyboardButton();

        button1.setText("\uD83D\uDCCCBosh Menyu\uD83D\uDCCC");
        button2.setText("♻\uFE0FQayta ro'yxatdan o'tish");
        button3.setText("\uD83D\uDE96Taxi buyurtma berish\uD83D\uDE96");

        button1.setCallbackData("Menu");
        button2.setCallbackData("userReRegister");
        button3.setCallbackData("taxiBuyurtma");

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        List<InlineKeyboardButton> row3 = new ArrayList<>();

        row1.add(button1);
        row2.add(button2);
        row3.add(button3);

        rowsInline.add(row1);
        rowsInline.add(row2);
        rowsInline.add(row3);

        markupInline.setKeyboard(rowsInline);

        return markupInline;
    }


    //TAXI BUYURTMA BERISH TUGMASI BOSILGANDA ISHLAYDI
    public void bookTaxi(Update update) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(getChatId(update));
        editMessageText.setText("\uD83D\uDEEBQayerdan qayerga bormoqchisiz \uD83D\uDEEC");
        editMessageText.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        editMessageText.setReplyMarkup(forBookTaxi());

        try {
            restTemplate.postForObject(RestConstants.EDIT_MESSAGE, editMessageText, Object.class);
            setUserState(longChatId(update), BotState.AT_TA);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private InlineKeyboardMarkup forBookTaxi() {

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();


        InlineKeyboardButton button1 = new InlineKeyboardButton();
        InlineKeyboardButton button2 = new InlineKeyboardButton();

        button1.setText("Andijon ➡\uFE0F Toshkent ");
        button2.setText("Toshkent ➡\uFE0F Andijon");

        button1.setCallbackData("AT");
        button2.setCallbackData("TA");

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();

        row1.add(button1);
        row2.add(button2);

        rowsInline.add(row1);
        rowsInline.add(row2);

        markupInline.setKeyboard(rowsInline);

        return markupInline;
    }


    public void getAndijonRegions(Update update) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(getChatId(update));
        editMessageText.setText("\uD83D\uDEEBQaysi tumandan yo'lga chiqmoqchisiz:");
        editMessageText.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        editMessageText.setReplyMarkup(forAndijonRegions());
        try {
            restTemplate.postForObject(RestConstants.EDIT_MESSAGE, editMessageText, Object.class);
            setUserState(longChatId(update), BotState.PASSENGER_COUNT_OR_DELIVERY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private InlineKeyboardMarkup forAndijonRegions() {

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();


        InlineKeyboardButton button1 = new InlineKeyboardButton();
        InlineKeyboardButton button2 = new InlineKeyboardButton();
        InlineKeyboardButton button3 = new InlineKeyboardButton();
        InlineKeyboardButton button4 = new InlineKeyboardButton();
        InlineKeyboardButton button5 = new InlineKeyboardButton();
        InlineKeyboardButton button6 = new InlineKeyboardButton();
        InlineKeyboardButton button7 = new InlineKeyboardButton();
        InlineKeyboardButton button8 = new InlineKeyboardButton();
        InlineKeyboardButton button9 = new InlineKeyboardButton();
        InlineKeyboardButton button10 = new InlineKeyboardButton();
        InlineKeyboardButton button11 = new InlineKeyboardButton();
        InlineKeyboardButton button12 = new InlineKeyboardButton();
        InlineKeyboardButton button13 = new InlineKeyboardButton();
        InlineKeyboardButton button14 = new InlineKeyboardButton();
        InlineKeyboardButton button15 = new InlineKeyboardButton();
        InlineKeyboardButton button16 = new InlineKeyboardButton();
        InlineKeyboardButton button17 = new InlineKeyboardButton();

        List<String> region = districtRepository.districtNameByRegion("Andijon");

        button1.setText(region.get(0));
        button1.setCallbackData(region.get(0));
        button2.setText(region.get(1));
        button2.setCallbackData(region.get(1));
        button3.setText(region.get(2));
        button3.setCallbackData(region.get(2));
        button4.setText(region.get(3));
        button4.setCallbackData(region.get(3));
        button5.setText(region.get(4));
        button5.setCallbackData(region.get(4));
        button6.setText(region.get(5));
        button6.setCallbackData(region.get(5));
        button7.setText(region.get(6));
        button7.setCallbackData(region.get(6));
        button8.setText(region.get(7));
        button8.setCallbackData(region.get(7));
        button9.setText(region.get(8));
        button9.setCallbackData(region.get(8));
        button10.setText(region.get(9));
        button10.setCallbackData(region.get(9));
        button11.setText(region.get(10));
        button11.setCallbackData(region.get(10));
        button12.setText(region.get(11));
        button12.setCallbackData(region.get(11));
        button13.setText(region.get(12));
        button13.setCallbackData(region.get(12));
        button14.setText(region.get(13));
        button14.setCallbackData(region.get(13));
        button15.setText(region.get(14));
        button15.setCallbackData(region.get(14));
        button16.setText(region.get(15));
        button16.setCallbackData(region.get(15));
        button17.setText(region.get(16));
        button17.setCallbackData(region.get(16));

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        List<InlineKeyboardButton> row3 = new ArrayList<>();
        List<InlineKeyboardButton> row4 = new ArrayList<>();
        List<InlineKeyboardButton> row5 = new ArrayList<>();
        List<InlineKeyboardButton> row6 = new ArrayList<>();

        row1.add(button1);
        row1.add(button2);
        row1.add(button3);
        row2.add(button4);
        row2.add(button5);
        row2.add(button6);
        row3.add(button7);
        row3.add(button8);
        row3.add(button9);
        row4.add(button10);
        row4.add(button11);
        row4.add(button12);
        row5.add(button13);
        row5.add(button14);
        row5.add(button15);
        row6.add(button16);
        row6.add(button17);

        rowsInline.add(row1);
        rowsInline.add(row2);
        rowsInline.add(row3);
        rowsInline.add(row4);
        rowsInline.add(row5);
        rowsInline.add(row6);

        markupInline.setKeyboard(rowsInline);

        return markupInline;
    }

    public void getToshkentRegions(Update update) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(getChatId(update));
        editMessageText.setText("\uD83D\uDEEBQaysi tumandan yo'lga chiqmoqchisiz:");
        editMessageText.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        editMessageText.setReplyMarkup(forToshkentRegions());
        try {
            restTemplate.postForObject(RestConstants.EDIT_MESSAGE, editMessageText, Object.class);
            setUserState(longChatId(update), BotState.PASSENGER_COUNT_OR_DELIVERY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private InlineKeyboardMarkup forToshkentRegions() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();


        InlineKeyboardButton button1 = new InlineKeyboardButton();
        InlineKeyboardButton button2 = new InlineKeyboardButton();
        InlineKeyboardButton button3 = new InlineKeyboardButton();
        InlineKeyboardButton button4 = new InlineKeyboardButton();
        InlineKeyboardButton button5 = new InlineKeyboardButton();
        InlineKeyboardButton button6 = new InlineKeyboardButton();
        InlineKeyboardButton button7 = new InlineKeyboardButton();
        InlineKeyboardButton button8 = new InlineKeyboardButton();
        InlineKeyboardButton button9 = new InlineKeyboardButton();
        InlineKeyboardButton button10 = new InlineKeyboardButton();
        InlineKeyboardButton button11 = new InlineKeyboardButton();
        InlineKeyboardButton button12 = new InlineKeyboardButton();

        List<String> region = districtRepository.districtNameByRegion("Toshkent");

        button1.setText(region.get(0));
        button1.setCallbackData(region.get(0));
        button2.setText(region.get(1));
        button2.setCallbackData(region.get(1));
        button3.setText(region.get(2));
        button3.setCallbackData(region.get(2));
        button4.setText(region.get(3));
        button4.setCallbackData(region.get(3));
        button5.setText(region.get(4));
        button5.setCallbackData(region.get(4));
        button6.setText(region.get(5));
        button6.setCallbackData(region.get(5));
        button7.setText(region.get(6));
        button7.setCallbackData(region.get(6));
        button8.setText(region.get(7));
        button8.setCallbackData(region.get(7));
        button9.setText(region.get(8));
        button9.setCallbackData(region.get(8));
        button10.setText(region.get(9));
        button10.setCallbackData(region.get(9));
        button11.setText(region.get(10));
        button11.setCallbackData(region.get(10));
        button12.setText(region.get(11));
        button12.setCallbackData(region.get(11));

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        List<InlineKeyboardButton> row3 = new ArrayList<>();
        List<InlineKeyboardButton> row4 = new ArrayList<>();
        List<InlineKeyboardButton> row5 = new ArrayList<>();

        row1.add(button1);
        row1.add(button2);
        row1.add(button3);
        row2.add(button4);
        row2.add(button5);
        row2.add(button6);
        row3.add(button7);
        row3.add(button8);
        row3.add(button9);
        row4.add(button10);
        row4.add(button11);
        row4.add(button12);

        rowsInline.add(row1);
        rowsInline.add(row2);
        rowsInline.add(row3);
        rowsInline.add(row4);
        rowsInline.add(row5);

        markupInline.setKeyboard(rowsInline);

        return markupInline;
    }


    //YO'LOVCHI BOSHLANISH TUMANNI TANLAGANDAN SO'NG ISHLAYDI
    public void countOrDelivery(Update update) {
        String district = update.getCallbackQuery().getData();
        String region = regionRepository.findByDistrictName(district);
        String chatId = getChatId(update);
        User user = userRepository.findByChatId(chatId);
        TourInfo tourInfo = TourInfo.builder().user(user).districtName(district).regionName(region).build();
        tourInfoRepository.save(tourInfo);
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(getChatId(update));
        editMessageText.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        editMessageText.setReplyMarkup(forCountOrDelivery());
        editMessageText.setText("\uD83D\uDC6BUmumiy necha kishisiz?\n" +
                "\uD83D\uDCE6Yoki dostavka xizmatidan foydalanasizmi?");
        try {
            restTemplate.postForObject(RestConstants.EDIT_MESSAGE, editMessageText, Object.class);
            setUserState(longChatId(update), BotState.TOUR_TIME);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private InlineKeyboardMarkup forCountOrDelivery() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();


        InlineKeyboardButton button1 = new InlineKeyboardButton();
        InlineKeyboardButton button2 = new InlineKeyboardButton();
        InlineKeyboardButton button3 = new InlineKeyboardButton();
        InlineKeyboardButton button4 = new InlineKeyboardButton();
        InlineKeyboardButton button5 = new InlineKeyboardButton();

        button1.setText("\uD83D\uDC6B1");
        button2.setText("\uD83D\uDC6B2");
        button3.setText("\uD83D\uDC6B3");
        button4.setText("\uD83D\uDC6B4");
        button5.setText("\uD83D\uDCE6Dostavka");

        button1.setCallbackData("1");
        button2.setCallbackData("2");
        button3.setCallbackData("3");
        button4.setCallbackData("4");
        button5.setCallbackData("Dostavka");

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row1.add(button1);
        row1.add(button2);
        row1.add(button3);
        row1.add(button4);
        row2.add(button5);

        rowsInline.add(row1);
        rowsInline.add(row2);

        markupInline.setKeyboard(rowsInline);

        return markupInline;
    }


    public void time(Update update) {
        String chatId = getChatId(update);
        String countOrDelivery = update.getCallbackQuery().getData();
        TourInfo tourInfo = tourInfoRepository.findTourInfosByUserChatIdOrderByCreatedAtDesc(chatId).get(0);
        tourInfo.setPeopleCountOrDelivery(countOrDelivery);
        tourInfoRepository.save(tourInfo);
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(getChatId(update));
        editMessageText.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        editMessageText.enableMarkdown(true);
        editMessageText.setText("\uD83D\uDCC6Safarni qanday vaqtga rejalashtirdingiz:\n" +
                "*Namuna: 15-02-2024, soat 15:00*");
        try {
            restTemplate.postForObject(RestConstants.EDIT_MESSAGE, editMessageText, Object.class);
            setUserState(longChatId(update), BotState.TOUR_INFO_FINISHED);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void tourCompleted(Update update) {
        String chatId = getChatId(update);
        TourInfo tourInfo = tourInfoRepository.findTourInfosByUserChatIdOrderByCreatedAtDesc(chatId).get(0);
        tourInfo.setDateTime(update.getMessage().getText());
        tourInfoRepository.save(tourInfo);

        SendMessage sendMessage = new SendMessage(chatId,
                "*Sizning so'rovingiz muvaffaqiyatli qabul qilindi✅*");
        sendMessage.enableMarkdown(true);

        try {
            sendMessage.setReplyMarkup(forTourCompleted());
            setUserState(longChatId(update), BotState.MENU_OR_DECLINE_ORDER_OR_CONTINUE);
            restTemplate.postForObject(RestConstants.FOR_MESSAGE, sendMessage, Object.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ReplyKeyboard forTourCompleted() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();


        InlineKeyboardButton button1 = new InlineKeyboardButton();
        InlineKeyboardButton button2 = new InlineKeyboardButton();

        button1.setText("✅Buyurtmani tasdiqlash va jo'natish✅");
        button2.setText("❌Buyurtmani bekor qilish❌");

        button1.setCallbackData("buyurtmaniTasdiqlash");
        button2.setCallbackData("buyurtmaniRadEtish");

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();

        row1.add(button1);
        row2.add(button2);

        rowsInline.add(row1);
        rowsInline.add(row2);

        markupInline.setKeyboard(rowsInline);

        return markupInline;
    }



    public void confirmBook(Update update) {
        String chatId = getChatId(update);
        TourInfo tourInfo = tourInfoRepository.findTourInfosByUserChatIdOrderByCreatedAtDesc(chatId).get(0);
        User user = userRepository.findByChatId(chatId);

        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(getChatId(update));
        editMessageText.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        editMessageText.enableMarkdown(true);
        editMessageText.setText("Tez orada haydovchilarimiz siz bilan bog'lanishadi\uD83D\uDE0A \n\n" +
                "*\uD83D\uDC47\uD83D\uDC47\uD83D\uDC47Safar ma'lumotlari\uD83D\uDC47\uD83D\uDC47\uD83D\uDC47 \n*" +
                "*\uD83D\uDC6BYo'lovchi ismi:  *" + user.getFirstName() + "\n" +
                "*\uD83D\uDCF1Yo'lovchining telefon raqami:  *" + user.getPhoneNumber() + "\n" +
                "*\uD83D\uDCCDSafar boshlanish nuqtasi:  *" + tourInfo.getDistrictName() + ",  " + tourInfo.getRegionName() + "\n" +
                "*\uD83D\uDC6B\uD83D\uDCE6Nechi kishi:  *" + tourInfo.getPeopleCountOrDelivery() + "\n" +
                "*\uD83D\uDCC6Taxminiy safar kuni va vaqti:  *" + tourInfo.getDateTime() + "\n");
        editMessageText.setReplyMarkup(forConfirmOrDeclineBook());

        SendMessage sendMessage1 = new SendMessage("-1002118305475",
                "*         \uD83D\uDD3BYangi buyurtma\uD83D\uDD3B*\n" +
                        "*\uD83D\uDC47\uD83D\uDC47\uD83D\uDC47Safar ma'lumotlari\uD83D\uDC47\uD83D\uDC47\uD83D\uDC47 \n*" +
                        "*\uD83D\uDC6BYo'lovchi ismi:  *" + user.getFirstName() + "\n" +
                        "*\uD83D\uDCF1Yo'lovchining telefon raqami:  *" + user.getPhoneNumber() + "\n" +
                        "*\uD83D\uDCCDSafar boshlanish nuqtasi:  *" + tourInfo.getDistrictName() + ",  " + tourInfo.getRegionName() + "\n" +
                        "*\uD83D\uDC6B\uD83D\uDCE6Nechi kishi:  *" + tourInfo.getPeopleCountOrDelivery() + "\n" +
                        "*\uD83D\uDCC6Taxminiy safar kuni va vaqti:  *" + tourInfo.getDateTime() + "\n");
        sendMessage1.enableMarkdown(true);

        try {
            restTemplate.postForObject(RestConstants.EDIT_MESSAGE, editMessageText, Object.class);
            restTemplate.postForObject(RestConstants.FOR_MESSAGE, sendMessage1, Object.class);
            setUserState(longChatId(update), BotState.PASSENGER_MENU);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private InlineKeyboardMarkup forConfirmOrDeclineBook() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        InlineKeyboardButton button1 = new InlineKeyboardButton();

        button1.setText("\uD83D\uDCCCBosh Menyu\uD83D\uDCCC");

        button1.setCallbackData("Menu");

        List<InlineKeyboardButton> row1 = new ArrayList<>();

        row1.add(button1);

        rowsInline.add(row1);

        markupInline.setKeyboard(rowsInline);

        return markupInline;
    }



    public void declineBook(Update update) {
        String chatId = getChatId(update);
        TourInfo tourInfo = tourInfoRepository.findTourInfosByUserChatIdOrderByCreatedAtDesc(chatId).get(0);
        tourInfoRepository.delete(tourInfo);

        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(getChatId(update));
        editMessageText.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        editMessageText.enableMarkdown(true);
        editMessageText.setText("*Buyurtmangiz bekor qilindi✅*");
        editMessageText.setReplyMarkup(forConfirmOrDeclineBook());

        try {
            restTemplate.postForObject(RestConstants.EDIT_MESSAGE, editMessageText, Object.class);
            setUserState(longChatId(update), BotState.PASSENGER_MENU);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static boolean isValidPhoneNumber(String phoneNumber) {
        String regex = "\\+998[1-9]\\d{8}";

        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(phoneNumber);

        return matcher.matches();
    }

}
