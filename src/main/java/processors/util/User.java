package processors.util;

import java.time.LocalDateTime;

public class User {
  private long userID;
  private String firstName;
  private String lastName;
  private String userName;
  private int userRating;
  private int totalMessages;
  private boolean isConfirmed;
  private LocalDateTime firstMessageTime;

  public User(long userID, String firstName, String lastName, String userName, boolean isOld) {
    this.userID = userID;
    this.firstName = firstName;
    this.lastName = lastName;
    this.userName = userName;
    this.isConfirmed = isOld;
    userRating = 0;
    totalMessages = 1;
  }

  public int getMessagesCount() {
    return totalMessages;
  }

  public User(long userID, String firstName, String lastName, String userName, boolean isOld, int userRating, int totalMessages) {
    this.userID = userID;
    this.firstName = firstName;
    this.lastName = lastName;
    this.userName = userName;
    this.isConfirmed = isOld;
    this.userRating = userRating;
    this.totalMessages = totalMessages;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public long getUserID() {
    return userID;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public boolean isConfirmed() {
    return isConfirmed;
  }


  public int getUserRating() {
    return userRating;
  }

  public int incUserRating() {
    userRating++;
    return userRating;
  }

  public int decUserRating() {
    userRating--;
    return userRating;
  }

  public void confirm() {
    isConfirmed = true;
  }

  public void addMessagesCount() {
    totalMessages++;
  }
}
