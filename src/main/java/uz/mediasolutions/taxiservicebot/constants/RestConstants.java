package uz.mediasolutions.taxiservicebot.constants;


public interface RestConstants {
    //REAL
    String BOT_TOKEN = "6704506589:AAEJjhWRoXfVwA5PUkSHEkg5mFB7AHUcSf8";

    //SAKAKA
//    String BOT_TOKEN = "6052104473:AAEscLILevwPMcG_00PYqAf-Kpb7eIUCIGg";

    String BOT_USERNAME = "Andijon_Toshkent1_bot";
    String URL = "https://api.telegram.org/bot";
    String FOR_MESSAGE = URL + BOT_TOKEN + "/sendMessage";
    String EDIT_MESSAGE = URL + BOT_TOKEN + "/editMessageText";
    String FORWARD = URL + BOT_TOKEN + "/forwardMessage";
    String FOR_DELETE_MESSAGE = URL + BOT_TOKEN + "/deleteMessage";
    String FOR_DOCUMENT = URL + BOT_TOKEN + "/sendDocument";

}
