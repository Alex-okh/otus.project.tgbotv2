package processors;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import processors.util.ChatTypes;
import processors.util.CommonDataHolder;
import processors.util.EventTypes;
import processors.util.UpdateData;
import tools.TextChecker;


public class PublicHelperProcessor implements FeatureProcessor {
  private static final Logger logger = LogManager.getLogger(PublicHelperProcessor.class);
  private CommonDataHolder commonData;

  public PublicHelperProcessor(CommonDataHolder commonData) {
    this.commonData = commonData;
  }

  @Override
  public boolean processFeature(UpdateData update, TelegramBot bot) {
    if (skipProcessing(update)) {
      return true;
    }
    String helpInfo = TextChecker.helpFound(update.getMessageText());

    if (helpInfo.isEmpty()) {
      return true;
    }
    SendMessage sm = new SendMessage(update.getChatId(),
                                     helpInfo).replyToMessageId(update.getMessageID());
    BaseResponse r = bot.execute(sm);
    if (!r.isOk()) {
      logger.error("Could not respond to user. Reason: {}",
                   r.description());
    }

    return true;
  }


  private boolean skipProcessing(UpdateData update) {
    if (update.getChatType() != ChatTypes.PUBLIC_CHAT || update.getEventType() != EventTypes.NEW_MESSAGE) {
      return true;
    }
    return false;
  }
}


