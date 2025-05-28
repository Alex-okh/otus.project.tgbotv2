package processors;

import com.google.gson.Gson;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import processors.util.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

public class SaveUpdateProcessor implements FeatureProcessor {
  private static final Logger logger = LogManager.getLogger(SaveUpdateProcessor.class);
  private final String SAVE_UPDATE_QUERY = "insert into updatedump (updateid, update) values (?, ?)";
  private final String SAVE_MESSAGE_QUERY = "insert into repeats (user_id,chat_id, msg_id,msg_time,msg_text,caption) values (?, ?, ?, ?, ?, ?)";
  private String updatesDump;
  private MyDBConnection db;

  public SaveUpdateProcessor() {
    updatesDump = "messagedump.txt";
    db = MyDBConnection.getInstance();
  }


  @Override
  public boolean processFeature(UpdateData update, TelegramBot bot) {
    db = MyDBConnection.getInstance();
    saveUpdateToDB(update.getRawUpdate());

    if ((update.getEventType() == EventTypes.NEW_MESSAGE || update.getEventType() == EventTypes.EDITED_MESSAGE) &&
        update.getChatType() == ChatTypes.PUBLIC_CHAT) {
      saveMessageToDB(update);
    }
    return true;
  }

  private void saveUpdateToDB(Update update) {
    int updateID = update.updateId();
    Gson gson = new Gson();
    String json = gson.toJson(update);

    try (PreparedStatement prs = db.dbconnection.prepareStatement(SAVE_UPDATE_QUERY)) {
      prs.setInt(1,
                 updateID);
      prs.setString(2,
                    json);
      prs.executeUpdate();
      logger.info("Update {} saved to database.",
                  updateID);
    } catch (SQLException ex) {
      logger.error("Error saving update object to DB. Error: {}",
                   ex.toString());
    }
  }

  private void saveMessageToDB(UpdateData update) {
    Options options = Options.getInstance();

    if (update.getMessageText().length() < options.getShortestMessageToCheck() &&
        update.getCaptionText().length() < options.getShortestMessageToCheck()) {
      return;
    }

    Timestamp time = Timestamp.from(Instant.ofEpochSecond(update.getMessageDate()));
    try (PreparedStatement prs = db.dbconnection.prepareStatement(SAVE_MESSAGE_QUERY)) {
      prs.setLong(1,
                  update.getSenderId());
      prs.setLong(2,
                  update.getChatId());
      prs.setInt(3,
                 update.getMessageID());
      prs.setTimestamp(4,
                       time);
      prs.setString(5,
                    update.getMessageText());
      prs.setString(6,
                    update.getCaptionText());
      prs.executeUpdate();
      logger.info("Message text from user {} saved to database.",
                  update.getSenderId());
    } catch (SQLException ex) {
      logger.error("Error saving text from user {} to DB. Error: {}",
                   update.getSenderId(),
                   ex.toString());
    }
  }
}

