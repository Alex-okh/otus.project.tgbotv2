package processors;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import processors.util.*;


public class PersonalCommandProcessor implements FeatureProcessor {
  private static final Logger logger = LogManager.getLogger(PersonalCommandProcessor.class);
  private CommonDataHolder commonData;

  public PersonalCommandProcessor(CommonDataHolder commonData) {
    this.commonData = commonData;
  }

  @Override
  public boolean processFeature(UpdateData update, TelegramBot bot) {


    if (skipProcessing(update)) {
      return true;
    }
    Options options = Options.getInstance();
    logger.info("command {} was sent to bot.",
                update.getMessageText());
    String reply = switch (update.getMessageText()) {
      case "/start" -> options.getMessageText("PRIVATE_HELLO").formatted(update.getUserName());
      case "/rules" -> options.getMessageText("RULES");
      case "/help" -> options.getMessageText("PRIVATE_HELP").formatted(update.getUserName());
      default -> "Не знаю такой команды, %s!".formatted(update.getUserName());
    };

    SendMessage sm = new SendMessage(update.getSenderId(),
                                     reply);
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


  private boolean skipProcessing(UpdateData update) {
    if (update.getEventType() == EventTypes.NEW_MESSAGE && update.getChatType() == ChatTypes.PRIVATE_CHAT &&
        (update.getMessageText().startsWith("/"))) {
      return false;
    }
    return true;
  }
}


