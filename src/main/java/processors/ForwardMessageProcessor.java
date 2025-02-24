package processors;

import com.google.gson.Gson;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.ForwardMessage;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import processors.util.*;


public class ForwardMessageProcessor implements FeatureProcessor {
  private static final Logger logger = LogManager.getLogger(ForwardMessageProcessor.class);
  private CommonDataHolder commonData;

  public ForwardMessageProcessor(CommonDataHolder commonData) {
    this.commonData = commonData;
  }

  @Override
  public boolean processFeature(UpdateData update, TelegramBot bot) {
    Options options = Options.getInstance();
    if (skipProcessing(update)) {
      return true;
    }

    if (update.getChatType() == ChatTypes.PRIVATE_CHAT) {
      ForwardMessage fm = new ForwardMessage(options.getAdminChatID(),
                                             update.getChatId(),
                                             update.getMessageID());
      Message forwardedMessage = bot.execute(fm).message();
      logger.info("Message from user id {} forwarded to admins: {}",
                  update.getSenderId(),
                  forwardedMessage.text());
      commonData.rememberForwarded(forwardedMessage.messageId().toString(),
                                   String.valueOf(update.getSenderId()));
    } else {
      String repliedMessageID = String.valueOf(update.getRawUpdate().message().replyToMessage().messageId());
      String originalSender = commonData.remindForwarded(repliedMessageID);
      if (originalSender != null) {
        SendMessage sm = new SendMessage(originalSender,
                                         update.getMessageText());
        BaseResponse response = bot.execute(sm);
        if (response.isOk()) {
          logger.info("Admin reply to user id {} was sent successfully.",
                      originalSender);
        } else {
          logger.warn("Admin reply to user id {} was not sent back. Reason: {}",
                      originalSender,
                      response.description());
        }
      } else {
        logger.error("Could not find mapping to reply: {}",
                     update.getMessageText());
      }
    }

    return false; // DO NOT CONTINUE PROCESSING
  }


  private boolean skipProcessing(UpdateData update) {
    if (update.getChatType() == ChatTypes.PUBLIC_CHAT || update.getEventType() != EventTypes.NEW_MESSAGE) {
      return true;
    }
    if (update.getMessageText().startsWith("/")) {
      return true;
    }
    //skip ADMIN CHAT messages which are NOT replies to forward.
    if (update.getChatType() == ChatTypes.ADMIN_CHAT && (update.getRawUpdate().message().replyToMessage() == null ||
                                                         update.getRawUpdate().message().replyToMessage().forwardOrigin() ==
                                                         null)) {
      return true;
    }

    return false;
  }
}


