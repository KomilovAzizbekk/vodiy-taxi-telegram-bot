package uz.mediasolutions.taxiservicebot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import uz.mediasolutions.taxiservicebot.payload.BotState;
import uz.mediasolutions.taxiservicebot.repository.DistrictRepository;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TgService {

    private final WebhookService webhookService;

    private final DriverService driverService;

    private final UserService userService;

    private final DistrictRepository districtRepository;


    public void getUpdate(Update update) throws IOException {

//        driverService.sendMessageToDrivers();

        if (update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            if (message.equals("/start")) {
                webhookService.whenStart(update);
            } else if (message.equals("/stat")) {
                webhookService.statistics(update);
            }
            //DRIVER
            else if (driverService.getUserState(chatId).equals(BotState.DRIVER_PHONE_1)) {
                driverService.driverPhone1(update);
            } else if (driverService.getUserState(chatId).equals(BotState.DRIVER_PHONE_2)) {
                driverService.driverPhone2(update);
            } else if (driverService.getUserState(chatId).equals(BotState.INCORRECT_DRIVER_PHONE_1)) {
                driverService.incorrectDriverPhone1(update);
            } else if (driverService.getUserState(chatId).equals(BotState.INCORRECT_DRIVER_PHONE_2)) {
                driverService.incorrectDriverPhone2(update);
            } else if (driverService.getUserState(chatId).equals(BotState.CAR_MODEL)) {
                driverService.carModel(update);
            } else if (driverService.getUserState(chatId).equals(BotState.CAR_NUMBER)) {
                driverService.carNumber(update);
            } else if (driverService.getUserState(chatId).equals(BotState.DRIVER_REGISTRATION_FINISHED)) {
                driverService.registrationFinished(update);
                webhookService.menuWithoutEdit(update);
            }
            // TODO TO'LOV QO'SHILSA ISHLAYDI
//            else if (driverService.getUserState(chatId).equals(BotState.SEND_SCREENSHOT)) {
//                driverService.notPhoto(update);
//            } else if (driverService.getUserState(chatId).equals(BotState.INCORRECT_PHOTO_SIZE)) {
//                driverService.incorrectPhotoSize(update);
//            }
            //USER
            else if (userService.getUserState(chatId).equals(BotState.PASSENGER_PHONE)) {
                userService.userPhone(update);
            } else if (userService.getUserState(chatId).equals(BotState.INCORRECT_USER_PHONE)) {
                userService.incorrectUserPhone(update);
            } else if (userService.getUserState(chatId).equals(BotState.PASSENGER_REGISTRATION_FINISHED)) {
                userService.registerDone(update);
            } else if (userService.getUserState(chatId).equals(BotState.TOUR_INFO_FINISHED)) {
                userService.tourCompleted(update);
            }
        } else if (update.hasMessage() && update.getMessage().hasContact()) {
            Long chatId = update.getMessage().getChatId();
            if (driverService.getUserState(chatId).equals(BotState.DRIVER_PHONE_2)) {
                driverService.driverPhone2(update);
            } else if (driverService.getUserState(chatId).equals(BotState.CAR_MODEL)) {
                driverService.carModel(update);
            } else if (userService.getUserState(chatId).equals(BotState.PASSENGER_REGISTRATION_FINISHED)) {
                userService.registerDone(update);
            } else if (userService.getUserState(chatId).equals(BotState.INCORRECT_USER_PHONE)) {
                userService.incorrectUserPhone(update);
            } else if (driverService.getUserState(chatId).equals(BotState.INCORRECT_DRIVER_PHONE_1)) {
                driverService.incorrectDriverPhone1(update);
            } else if (driverService.getUserState(chatId).equals(BotState.INCORRECT_DRIVER_PHONE_2)) {
                driverService.incorrectDriverPhone2(update);
            }
            // TODO TO'LOV QO'SHILSA ISHLAYDI
//            else if (driverService.getUserState(chatId).equals(BotState.INCORRECT_PHOTO_SIZE)) {
//                driverService.incorrectPhotoSize(update);
//            }
        } else if (update.hasCallbackQuery()) {
            List<String> strings = districtRepository.districtName();
            String data = update.getCallbackQuery().getData();
            Long chatId = update.getCallbackQuery().getMessage().getChatId();

            //DRIVER
            if (webhookService.getUserState(chatId).equals(BotState.CHOOSE_ROLE) &&
                    data.equals("Haydovchi")) {
                driverService.saveDriver(update);
            } else if ((driverService.getUserState(chatId).equals(BotState.MENU_OR_CONFIRM_PAYMENT) ||
                    userService.getUserState(chatId).equals(BotState.MENU_OR_TAXI_BOOK) ||
                    userService.getUserState(chatId).equals(BotState.PASSENGER_MENU) ||
                    driverService.getUserState(chatId).equals(BotState.DRIVER_MENU)) &&
                    data.equals("Menu")) {
                webhookService.menu(update);
            } else if (driverService.getUserState(chatId).equals(BotState.BACK_OR_CONFIRM_PAYMENT) &&
                    data.equals("driverReRegister")) {
                driverService.driverReRegister(update);
            } else if (webhookService.getUserState(chatId).equals(BotState.FOR_ADMINS) &&
                        data.equals("adminMenu")) {
                webhookService.menuWithDelete(update);
                webhookService.menuWithoutEdit(update);
            }
            // TODO TO'LOV QO'SHILSA ISHLAYDI
//            else if ((driverService.getUserState(chatId).equals(BotState.MENU_OR_CONFIRM_PAYMENT) ||
//                    driverService.getUserState(chatId).equals(BotState.BACK_OR_CONFIRM_PAYMENT)) &&
//                    data.equals("Tolovni tasdiqlash")) {
//                driverService.sendPayment(update);
//            }
            else if (driverService.getUserState(chatId).equals(BotState.MENU_OR_CONFIRM_PAYMENT) &&
                    data.equals("yo'lovchi")) {
                userService.saveUser(update);
            }
            // TODO TO'LOV QO'SHILSA ISHLAYDI
//            if (data.startsWith("accept")) {
//                System.out.println(data);
//                System.out.println(data.substring(6));
//                driverService.acceptScreenshot(update, data.substring(6));
//            } else if (data.startsWith("reject")) {
//                driverService.rejectScreenshot(update, data.substring(6));
//            }
            //USER
            else if (webhookService.getUserState(chatId).equals(BotState.CHOOSE_ROLE) &&
                    data.equals("Yo'lovchi")) {
                userService.saveUser(update);
            } else if (userService.getUserState(chatId).equals(BotState.AT_TA) &&
                    data.equals("AT")) {
                userService.getAndijonRegions(update);
            } else if (userService.getUserState(chatId).equals(BotState.AT_TA) &&
                    data.equals("TA")) {
                userService.getToshkentRegions(update);
            } else if (userService.getUserState(chatId).equals(BotState.PASSENGER_COUNT_OR_DELIVERY) &&
                    strings.contains(data)) {
                userService.countOrDelivery(update);
            } else if ((userService.getUserState(chatId).equals(BotState.BACK_OR_TAXI_BOOK) ||
                    userService.getUserState(chatId).equals(BotState.MENU_OR_TAXI_BOOK)) &&
                    data.equals("taxiBuyurtma")) {
                userService.bookTaxi(update);
            } else if (userService.getUserState(chatId).equals(BotState.TOUR_TIME) &&
                    (data.equals("1") || data.equals("2") || data.equals("3") ||
                            data.equals("4") || data.equals("Dostavka"))) {
                userService.time(update);
            } else if (userService.getUserState(chatId).equals(BotState.PASSENGER_MENU) &&
                    data.equals("haydovchi")) {
                driverService.saveDriver(update);
            } else if (userService.getUserState(chatId).equals(BotState.MENU_OR_DECLINE_ORDER_OR_CONTINUE) &&
                    data.startsWith("buyurtmaniRadEtish")) {
                userService.declineBook(update);
            } else if (userService.getUserState(chatId).equals(BotState.MENU_OR_DECLINE_ORDER_OR_CONTINUE) &&
                    data.equals("buyurtmaniTasdiqlash")) {
                userService.confirmBook(update);
            } else if (driverService.getUserState(chatId).equals(BotState.BACK_OR_CONFIRM_PAYMENT) &&
                    data.equals("OrtgaDriver")) {
                webhookService.menu(update);
            } else if (userService.getUserState(chatId).equals(BotState.BACK_OR_TAXI_BOOK) &&
                    data.equals("OrtgaUser")) {
                webhookService.menu(update);
            } else if (webhookService.getUserState(chatId).equals(BotState.FOR_ADMINS) &&
                    data.equals("driverList")) {
                webhookService.driverList(update);
            } else if (webhookService.getUserState(chatId).equals(BotState.FOR_ADMINS) &&
                    data.equals("userList")) {
                webhookService.userList(update);
            } else if ((userService.getUserState(chatId).equals(BotState.MENU_OR_TAXI_BOOK) ||
                    userService.getUserState(chatId).equals(BotState.BACK_OR_TAXI_BOOK)) &&
                    data.equals("userReRegister")) {
                userService.userReRegister(update);
            }
        }
        // TODO TO'LOV QO'SHILSA ISHLAYDI
//        else if (update.hasMessage() && update.getMessage().hasPhoto()) {
//            Long chatId = update.getMessage().getChatId();
//            System.out.println("Size: " + update.getMessage().getPhoto().size());
//            if (driverService.getUserState(chatId).equals(BotState.SEND_SCREENSHOT)) {
//                driverService.sendPhotoToChannel(update);
//            } else if (driverService.getUserState(chatId).equals(BotState.INCORRECT_PHOTO_SIZE)) {
//                driverService.incorrectPhotoSize(update);
//            }
//        } else if (update.hasMessage() && !update.getMessage().hasPhoto()) {
//            Long chatId = update.getMessage().getChatId();
//            if (driverService.getUserState(chatId).equals(BotState.SEND_SCREENSHOT)) {
//                driverService.notPhoto(update);
//            } else if (driverService.getUserState(chatId).equals(BotState.INCORRECT_PHOTO_SIZE)) {
//                driverService.incorrectPhotoSize(update);
//            }
//        }
    }
}
