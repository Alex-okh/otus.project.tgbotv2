package processors;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import processors.util.*;
import tools.TextChecker;


public class AdminCommandProcessor implements FeatureProcessor {
  private static final Logger logger = LogManager.getLogger(AdminCommandProcessor.class);
  private CommonDataHolder commonData;
  private Options options;

  public AdminCommandProcessor(CommonDataHolder commonData) {
    this.commonData = commonData;
    options = Options.getInstance();
  }

  @Override
  public boolean processFeature(UpdateData update, TelegramBot bot) {
    if (skipProcessing(update)) {
      return true;
    }
    logger.info("command {} was sent to admin chat.",
                update.getMessageText());
    String oldAdminChat = options.getAdminChatID();
    String command = update.getMessageText().split(" ")[0];
    String reply;
    switch (command) {
      case "-help" -> reply = options.getMessageText("ADMIN_HELP").formatted(update.getUserName());
      case "-rules" -> reply = options.getMessageText("RULES");
      case "-setrules" -> reply = SetRules(update.getMessageText());
      case "-set_admin_chat" -> reply = setAdminChat(update);
      case "-clear_admin_chat" -> reply = clearAdminChat();
      case "-set_ban_rating" -> reply = setBanRating(update.getMessageText());
      case "-save_settings" -> reply = saveSettings();
      case "-add_explicit" -> reply = addExplicit(update.getMessageText());
      case "-add_helper" -> reply = addHelper(update.getMessageText());

      default -> reply = "Не знаю такой команды, %s!".formatted(update.getUserName());
    }
    String adminChat = options.getAdminChatID().isEmpty() ? oldAdminChat : options.getAdminChatID();
    SendMessage sm = new SendMessage(adminChat,
                                     reply).replyToMessageId(update.getMessageID());
    BaseResponse response = bot.execute(sm);
    if (response.isOk()) {
      logger.info("Bot reply to user id {} was sent successfully.",
                  update.getSenderId());
    } else {
      logger.warn("Bot reply to user id {} was not sent back. Reason: {}",
                  update.getSenderId(),
                  response.description());
    }

    return false; // DO NOT CONTINUE PROCESSING
  }

  private String SetRules(String message) {
    String newRules = message.substring(message.indexOf(" "));
    options.setMessageText("RULES",
                           newRules);
    return "OK.";
  }

  private String setAdminChat(UpdateData update) {
    if (options.getAdminChatID().isEmpty()) {
      options.setAdminChatID(String.valueOf(update.getChatId()));
      return "Админ-чат установлен. ID чата = %s, название: %s".formatted(options.getAdminChatID(),
                                                                          update.getRawUpdate().message().chat().title());

    } else {
      return "Админ-чат уже задан.";
    }

  }

  private String clearAdminChat() {
    options.setAdminChatID("");
    return "Админ-чат сброшен.";
  }

  private String setBanRating(String message) {
    String[] split = message.split(" ");
    if (split.length < 2) {
      return "Не найдено число. На сколько ставить-то?";
    }
    int newThreshold;
    try {
      newThreshold = Integer.parseInt(split[1]);
      options.setBanThreshold(newThreshold);
      return "Новый порог бана установлен.";
    } catch (NumberFormatException e) {
      return "Неверный формат числа.";
    }
  }

  private String saveSettings() {
    options.saveOptions("default.ini");
    options.saveMessages("messages.ini");
    return "Настройки сохранены";
  }

  private String addExplicit(String message) {
    TextChecker.addToDictionary(message.substring(message.indexOf(" ")));
    return "OK.";
  }

  private String addHelper(String message) {
    TextChecker.addHelper(message.substring(message.indexOf(" ")));
    return "OK.";
  }


  private boolean skipProcessing(UpdateData update) {
    if (update.getMessageText().equals("-set_admin_chat") && options.getAdminChatID().isEmpty()) {
      return false;
    }
    if (update.getEventType() == EventTypes.NEW_MESSAGE && update.getChatType() == ChatTypes.ADMIN_CHAT && (update.getMessageText().startsWith("-"))) {
      return false;
    }
    return true;
  }
}


