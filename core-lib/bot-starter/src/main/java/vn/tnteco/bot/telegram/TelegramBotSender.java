package vn.tnteco.bot.telegram;

public interface TelegramBotSender {

    void sendMessage(String botToken, String chatId, String message);

}
