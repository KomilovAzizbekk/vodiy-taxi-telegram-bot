package uz.mediasolutions.taxiservicebot.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Table;
import lombok.RequiredArgsConstructor;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import uz.mediasolutions.taxiservicebot.constants.RestConstants;
import uz.mediasolutions.taxiservicebot.entity.Driver;
import uz.mediasolutions.taxiservicebot.entity.User;
import uz.mediasolutions.taxiservicebot.payload.BotState;
import uz.mediasolutions.taxiservicebot.repository.DriverRepository;
import uz.mediasolutions.taxiservicebot.repository.UserRepository;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WebhookService {

    private final RestTemplate restTemplate;

    private final UserRepository userRepository;

    private final DriverRepository driverRepository;


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


    public void whenStart(Update update) {
        SendMessage sendMessage = new SendMessage(getChatId(update),
                "Assalomu alaykum hurmatli " + update.getMessage().getFrom().getFirstName() +
                        "\uD83D\uDE0A.\nVodiy Toshkent yo'nalishidagi taksi xizmatini taqdim etuvchi botga xush kelibsiz \uD83D\uDE95\n" +
                        "Bot xizmatidan foydalanish uchun avvalo ro'yxatdan o'tishingiz zarur.\n" +
                        "Botdan kim sifatida foydalanmoqchisiz?");
        sendMessage.setReplyMarkup(forStart());
        try {
            restTemplate.postForObject(RestConstants.FOR_MESSAGE, sendMessage, Object.class);
            setUserState(longChatId(update), BotState.CHOOSE_ROLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private InlineKeyboardMarkup forStart() {

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();


        InlineKeyboardButton button1 = new InlineKeyboardButton();
        InlineKeyboardButton button2 = new InlineKeyboardButton();

        button1.setText("\uD83D\uDE96Haydovchi");
        button2.setText("\uD83E\uDD35\uD83C\uDFFD\u200D♂\uFE0FYo'lovchi");

        button1.setCallbackData("Haydovchi");
        button2.setCallbackData("Yo'lovchi");

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(button1);
        row1.add(button2);

        rowsInline.add(row1);

        markupInline.setKeyboard(rowsInline);

        return markupInline;
    }


    public void menu(Update update) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(getChatId(update));
        editMessageText.setText("Vodiy Toshkent yo'nalishidagi taksi xizmati. \uD83D\uDE95\n" +
                "Bot xizmatidan kim sifatida foydalanmoqchisiz?");
        editMessageText.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        editMessageText.setReplyMarkup(forMenu());
        try {
            restTemplate.postForObject(RestConstants.EDIT_MESSAGE, editMessageText, Object.class);
            setUserState(longChatId(update), BotState.CHOOSE_ROLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void menuWithoutEdit(Update update) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(getChatId(update));
        sendMessage.setText("Vodiy Toshkent yo'nalishidagi taksi xizmati. \uD83D\uDE95\n" +
                "Bot xizmatidan kim sifatida foydalanmoqchisiz?");
        sendMessage.setReplyMarkup(forMenu());
        try {
            restTemplate.postForObject(RestConstants.FOR_MESSAGE, sendMessage, Object.class);
            setUserState(longChatId(update), BotState.CHOOSE_ROLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void menuWithDelete(Update update) {

        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(getChatId(update));
        deleteMessage.setMessageId(update.getCallbackQuery().getMessage().getMessageId());

        try {
            restTemplate.postForObject(RestConstants.FOR_DELETE_MESSAGE, deleteMessage, Object.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private InlineKeyboardMarkup forMenu() {

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();


        InlineKeyboardButton button1 = new InlineKeyboardButton();
        InlineKeyboardButton button2 = new InlineKeyboardButton();

        button1.setText("\uD83D\uDE96Haydovchi");
        button2.setText("\uD83E\uDD35\uD83C\uDFFD\u200D♂\uFE0FYo'lovchi");

        button1.setCallbackData("Haydovchi");
        button2.setCallbackData("Yo'lovchi");

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(button1);
        row1.add(button2);

        rowsInline.add(row1);

        markupInline.setKeyboard(rowsInline);

        return markupInline;
    }


    public void statistics(Update update) {
        long drivers = driverRepository.count();
        long users = userRepository.count();
        String chatId = getChatId(update);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        if (chatId.equals("5563270759") || chatId.equals("285710521") || chatId.equals("1302908674")) {
            String text = "\uD83D\uDE96Haydovchilar soni - " + drivers + " ta.\n" +
                    "\uD83E\uDD35\uD83C\uDFFD\u200D♂\uFE0FYo'lovchilar soni - " + users + " ta.\n" +
                    "Ro'yxatni pdf faylda yuklash uchun pastdagi tugmalarni bosing.";
            sendMessage.setText(text);
            sendMessage.setReplyMarkup(forStatistics());
        } else {
            sendMessage.setText("Ushbu buyruq faqat administratorlar uchun.");
        }
        try {
            restTemplate.postForObject(RestConstants.FOR_MESSAGE, sendMessage, Object.class);
            setUserState(longChatId(update), BotState.FOR_ADMINS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ReplyKeyboard forStatistics() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();


        InlineKeyboardButton button1 = new InlineKeyboardButton();
        InlineKeyboardButton button2 = new InlineKeyboardButton();
        InlineKeyboardButton button3 = new InlineKeyboardButton();

        button1.setText("\uD83D\uDE96Haydovchilar ro'yxati");
        button2.setText("\uD83E\uDD35\uD83C\uDFFD\u200D♂\uFE0FYo'lovchilar ro'yxati");
        button3.setText("\uD83D\uDCCCBosh Menyu\uD83D\uDCCC");

        button1.setCallbackData("driverList");
        button2.setCallbackData("userList");
        button3.setCallbackData("adminMenu");

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



    private void generateDrivers() throws FileNotFoundException {
        List<Driver> drivers = driverRepository.findAll();
        PdfWriter pdfWriter = new PdfWriter("file/haydovchilar.pdf");
        PdfDocument pdfDocument = new PdfDocument(pdfWriter);
        pdfDocument.addNewPage();
        Document document = new Document(pdfDocument);
        float[] width = {100F, 100F, 100F, 100F, 100F};
        Table table = new Table(width);
        table.addCell("FIO");
        table.addCell("1-Telefon raqam");
        table.addCell("2-Telefon raqam");
        table.addCell("Avtomobil markasi");
        table.addCell("Avtomobil raqami");
        for (Driver driver : drivers) {
            if (driver != null) {
                table.addCell(driver.getFIO() != null ? driver.getFIO() : "");
                table.addCell(driver.getPhoneNumber1() != null ? driver.getPhoneNumber1() : "");
                table.addCell(driver.getPhoneNumber2() != null ? driver.getPhoneNumber2() : "");
                table.addCell(driver.getCar() != null ? driver.getCar().getModel() : "");
                table.addCell(driver.getCar() != null ? driver.getCar().getNumber() : "");
            }
        }
        document.add(table);
        document.close();
    }


    private void generateUsers() throws FileNotFoundException {
        List<User> users = userRepository.findAll();
        PdfWriter pdfWriter = new PdfWriter("file/yolovchilar.pdf");
        PdfDocument pdfDocument = new PdfDocument(pdfWriter);
        pdfDocument.addNewPage();
        Document document = new Document(pdfDocument);
        float[] width = {100F, 100F};
        Table table = new Table(width);
        table.addCell("Ism");
        table.addCell("Telefon raqam");
        for (User user : users) {
            if (user != null) {
                table.addCell(user.getFirstName() != null ? user.getFirstName() : "");
                table.addCell(user.getPhoneNumber() != null ? user.getPhoneNumber() : "");
            }
        }
        document.add(table);
        document.close();

    }


    public void driverList(Update update) throws FileNotFoundException {
        generateDrivers();
        HttpPost httppost = new HttpPost(RestConstants.FOR_DOCUMENT);
        InputFile inputFile = new InputFile(new File("file/haydovchilar.pdf"));

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addTextBody("chat_id", getChatId(update));
        builder.addBinaryBody(inputFile.getMediaName(), inputFile.getNewMediaFile(),
                ContentType.APPLICATION_OCTET_STREAM, inputFile.getMediaName());
        builder.addTextBody("document", inputFile.getAttachName());
        org.apache.http.HttpEntity multipart = builder.build();

        httppost.setEntity(multipart);
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            httpClient.execute(httppost);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    public void userList(Update update) throws FileNotFoundException {
        generateUsers();
        HttpPost httppost = new HttpPost(RestConstants.FOR_DOCUMENT);
        InputFile inputFile = new InputFile(new File("file/yolovchilar.pdf"));

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addTextBody("chat_id", getChatId(update));
        builder.addBinaryBody(inputFile.getMediaName(), inputFile.getNewMediaFile(),
                ContentType.APPLICATION_OCTET_STREAM, inputFile.getMediaName());
        builder.addTextBody("document", inputFile.getAttachName());
        org.apache.http.HttpEntity multipart = builder.build();

        httppost.setEntity(multipart);
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            httpClient.execute(httppost);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}








