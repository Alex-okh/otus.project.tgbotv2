package processors;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.SendResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import processors.util.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CheckRepeatsProcessor implements FeatureProcessor {
  private static final Logger logger = LogManager.getLogger(CheckRepeatsProcessor.class);
  private final String GET_MESSAGES_QUERY = "select msg_text, caption from repeats where user_id = ? and chat_id = ? and msg_time > (now() - interval '6 days');";
  private MyDBConnection db;
  private CommonDataHolder commonData;

  public CheckRepeatsProcessor(CommonDataHolder commonData) {
    this.commonData = commonData;
    db = MyDBConnection.getInstance();
  }

  @Override
  public boolean processFeature(UpdateData update, TelegramBot bot) {
    if (skipProcessing(update)) {
      return true;
    }
    List<String> storedMessages = getStoredMessagesFromDB(update.getSenderId(),
                                                          update.getChatId());

    boolean dublicatesFound = false;
    for (String text : storedMessages) {
      if ((text.equals(update.getMessageText()) || text.equals(update.getCaptionText())) && !text.isEmpty()) {
        dublicatesFound = true;
        break;
      }
    }
    //HOTFIX EDITED_MESSAGE Разобраться что за апдейты прилетают с пустой коректировкой.
    if (dublicatesFound && update.getEventType() != EventTypes.EDITED_MESSAGE) {
      int threadID = update.getMessageThreadId();
      SendMessage sm = new SendMessage(update.getChatId(),
                                       "%s, повторы не чаще раза в неделю!".formatted(update.getTagUserName())).messageThreadId(threadID);
      logger.info("Deleting repeated message from {} . ",
                  update.getSenderId());
      DeleteMessage dm = new DeleteMessage(update.getChatId(),
                                           update.getMessageID());
      SendResponse r = bot.execute(sm);
      DeleteMessage delayedDelete = new DeleteMessage(update.getChatId(), r.message().messageId());
      commonData.addDelayedMessage(delayedDelete,30);
      if (!r.isOk()) {
        logger.error("Error sending message. Reason: {}",
                     r.description());
      }
      BaseResponse dr = bot.execute(dm);
      if (!r.isOk()) {
        logger.error("Error deleting message. Reason: {}",
                     dr.description());
      }
    }

    return true;
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

  private List<String> getStoredMessagesFromDB(long userId, long chatId) {
    ArrayList<String> messages = new ArrayList<>();

    try (PreparedStatement prs = db.dbconnection.prepareStatement(GET_MESSAGES_QUERY)) {
      prs.setLong(1,
                  userId);
      prs.setLong(2,
                  chatId);
      ResultSet rs = prs.executeQuery();
      while (rs.next()) {
        String messageText = rs.getString("msg_text");
        String captionText = rs.getString("caption");
        if (messageText != null && !messageText.isEmpty()) {
          messages.add(messageText);
        }
        if (captionText != null && !captionText.isEmpty()) {
          messages.add(captionText);
        }
      }
      logger.info("Loaded {} messages from DB for userId {} and chatId {}",
                  messages.size(),
                  userId,
                  chatId);
    } catch (SQLException ex) {
      logger.error("Error loading messages from DB. Error: {}",
                   ex.toString());
    }
    return messages;
  }
}

