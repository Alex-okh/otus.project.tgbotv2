package processors;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.BanChatMember;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.SendResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import processors.util.*;
import tools.TextChecker;


public class CheckExplicitProcessor implements FeatureProcessor {
  private static final Logger logger = LogManager.getLogger(CheckExplicitProcessor.class);

  private CommonDataHolder commonData;

  public CheckExplicitProcessor(CommonDataHolder commonData) {
    this.commonData = commonData;
  }

  @Override
  public boolean processFeature(UpdateData update, TelegramBot bot) {
    Options options = Options.getInstance();
    if (skipProcessing(update)) {
      return true;
    }
    if (TextChecker.isTextOK(update.getMessageText()) && TextChecker.isTextOK(update.getCaptionText())) {
      return true;
    }

    SendMessage sm = new SendMessage(update.getChatId(),
                                     "%s, не ругайся! ".formatted(update.getTagUserName()));
    int userRating = commonData.decUserRating(update.getSenderId());
    logger.info("User {} rating decreased. Current rating = {}.",
                update.getSenderId(),
                userRating);
    DeleteMessage dm = new DeleteMessage(update.getChatId(),
                                         update.getMessageID());
    SendResponse sr = bot.execute(sm);
    DeleteMessage delayedDelete = new DeleteMessage(update.getChatId(), sr.message().messageId());
    commonData.addDelayedMessage(delayedDelete,15);

    bot.execute(dm);
    if (userRating < options.getBanThreshold()) {
      BanChatMember bm = new BanChatMember(update.getChatId(),
                                           update.getSenderId());
      BaseResponse banResponse = bot.execute(bm);
      if (banResponse.isOk()) {
        logger.info("User {} banned successfully.",
                    update.getSenderId());
      } else {
        logger.warn("Tried to ban user {}, but failed. Reason: {}",
                    update.getSenderId(),
                    banResponse.description());
      }
    }
    return false;
  }


  private boolean skipProcessing(UpdateData update) {
    if (update.getChatType() == ChatTypes.PRIVATE_CHAT || update.getChatType() == ChatTypes.ADMIN_CHAT) {
      return true;
    }
    if (update.getEventType() != EventTypes.NEW_MESSAGE && update.getEventType() != EventTypes.EDITED_MESSAGE) {
      return true;
    }
    return false;
  }
}


