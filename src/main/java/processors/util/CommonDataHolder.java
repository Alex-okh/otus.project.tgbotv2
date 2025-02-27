package processors.util;

import com.pengrad.telegrambot.request.DeleteMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class CommonDataHolder {
  private static final Logger logger = LogManager.getLogger(CommonDataHolder.class);
  private MyDBConnection db;
  private Map<String, String> forwardedFromPrivate;
  private Map<Long, User> knownPublicUsers;
  private SortedMap<Date, DeleteMessage> deleteQueue;

  public CommonDataHolder() {
    knownPublicUsers = new HashMap<>();
    forwardedFromPrivate = new HashMap<>();
    db = MyDBConnection.getInstance();
    deleteQueue = new TreeMap<>();
    loadDatafromDB();
  }


  public void rememberForwarded(String messageID, String senderID) {
    forwardedFromPrivate.put(messageID,
                             senderID);
  }

  public String remindForwarded(String messageID) {
    return forwardedFromPrivate.get(messageID);
  }

  public void addUser(long userID, String userFirstName, String userLastName, String userName, boolean isOld) {
    String SAVE_USER_QUERY = "insert into user_data (user_id,firstname, lastname,username,rating,isconfirmed,totalmessages) values (?, ?, ?, ?, ?, ?,?)";

    User newUser = new User(userID,
                            userFirstName,
                            userLastName,
                            userName,
                            isOld);
    knownPublicUsers.put(userID,
                         newUser);

    try (PreparedStatement prs = db.dbconnection.prepareStatement(SAVE_USER_QUERY)) {
      prs.setLong(1, newUser.getUserID());
      prs.setString(2, newUser.getFirstName());
      prs.setString(3, newUser.getLastName());
      prs.setString(4, newUser.getUserName());
      prs.setInt(5,newUser.getUserRating());
      prs.setBoolean(6, newUser.isConfirmed());
      prs.setInt(7, newUser.getMessagesCount());

      prs.executeUpdate();
      logger.info("New user id {} saved to database.",
                  userID);
    } catch (SQLException ex) {
      logger.error("Error saving new user id {} to DB. Error: {}",
                   userID,
                   ex.toString());
    }

  }

  public boolean isUserKnown(long userID) {
    return knownPublicUsers.containsKey(userID);
  }

  public boolean isUserConfirmed(long userID) {
    return (knownPublicUsers.containsKey(userID) && (knownPublicUsers.get(userID).isConfirmed()));
  }

  public void addMessageCount(long userID) {
    User user = knownPublicUsers.get(userID);
    user.addMessagesCount();
    String ADD_MSG_COUNT_QUERY = "UPDATE user_data SET totalmessages = totalmessages + 1 WHERE user_id=?";
    try (PreparedStatement prs = db.dbconnection.prepareStatement(ADD_MSG_COUNT_QUERY)) {
      prs.setLong(1,
                  userID);
      prs.executeUpdate();
      logger.info("User {} message counted.",
                  userID);
    } catch (SQLException ex) {
      logger.error("Error adding message count for {} in DB. Error: {}",
                   userID,
                   ex.toString());
    }

  }

  public int incUserRating(long userID) {
    User user = knownPublicUsers.get(userID);
    return user.incUserRating();
  }

  public int decUserRating(long userID) {
    logger.info("decUserRating(userID) {}",
                userID);

    User user = knownPublicUsers.get(userID);
    String DECREASE_RATING_QUERY = "UPDATE user_data SET rating = rating - 1 WHERE user_id=?";
    try (PreparedStatement prs = db.dbconnection.prepareStatement(DECREASE_RATING_QUERY)) {
      prs.setLong(1,
                  userID);
      prs.executeUpdate();
      logger.info("User {} rating decreased in database.",
                  userID);
    } catch (SQLException ex) {
      logger.error("Error decreasing rating for user id {} in DB. Error: {}",
                   userID,
                   ex.toString());
    }
    return user.decUserRating();

  }

  public void approveUser(long userID) {
    User user = knownPublicUsers.get(userID);
    user.confirm();
    String APPROVE_QUERY = "UPDATE user_data SET isconfirmed = true WHERE user_id=?";
    try (PreparedStatement prs = db.dbconnection.prepareStatement(APPROVE_QUERY)) {
      prs.setLong(1,
                  userID);
      prs.executeUpdate();
      logger.info("User {} approved in database.",
                  userID);
    } catch (SQLException ex) {
      logger.error("Error approving user id {} in DB. Error: {}",
                   userID,
                   ex.toString());
    }

  }

  private void loadDatafromDB() {
    try (PreparedStatement prs = db.dbconnection.prepareStatement("select * from user_data")) {
      ResultSet rs = prs.executeQuery();
      int totalUsers = 0;
      while (rs.next()) {
        User newUser = new User(rs.getLong("user_id"),
                                rs.getString("firstname"),
                                rs.getString("lastname"),
                                rs.getString("username"),
                                rs.getBoolean("isconfirmed"),
                                rs.getInt("rating"),
                                rs.getInt("totalmessages"));

        knownPublicUsers.put(newUser.getUserID(),
                             newUser);
        totalUsers++;
      }
      logger.info("Loaded {} users from DB. ",
                  totalUsers);
    } catch (SQLException ex) {
      logger.error("Error loading users from DB. Error: {}",
                   ex.toString());
    }
  }

  public synchronized void addDelayedMessage(DeleteMessage message, int delaySeconds) {
    Date deleteDate = new Date(new Date().getTime() + delaySeconds * 1000);
    deleteQueue.put(deleteDate,
                    message);
  }

  public synchronized Date getNextDeletedTime() {
    if (deleteQueue.isEmpty()) {
      return null;
    }
    return deleteQueue.firstKey();
  }

  public synchronized DeleteMessage getDelayedMessage() {
    return deleteQueue.remove(deleteQueue.firstKey());
  }
}

