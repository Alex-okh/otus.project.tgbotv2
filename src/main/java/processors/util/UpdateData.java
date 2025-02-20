package processors.util;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;

public class UpdateData {
  private EventTypes eventType = EventTypes.OTHER;
  private ChatTypes chatType;
  private int messageID;
  private int messageThreadId;
  private int msgDate;
  private long senderId;
  private long chatId;
  private String messageText = "";
  private String captionText = "";
  private String userName = "";
  private long newUserId;
  private String newUserName = "";
  private String callBackData = "";
  private final Update rawUpdate;

  public UpdateData(Update rawUpdate) {
    this.rawUpdate = rawUpdate;
    setChatType();

    if (rawUpdate.message() != null && rawUpdate.message().newChatMembers() != null) {
      eventType = EventTypes.NEW_CHAT_MEMBER;
      messageID = rawUpdate.message().messageId();
      messageThreadId = 0;
      msgDate = rawUpdate.message().date();
      senderId = rawUpdate.message().from().id();
      chatId = rawUpdate.message().chat().id();
      newUserId = rawUpdate.message().newChatMembers()[0].id();
      String fn = rawUpdate.message().newChatMembers()[0].firstName() == null ? "" : rawUpdate.message().newChatMembers()[0].firstName();
      String ln = rawUpdate.message().newChatMembers()[0].lastName() == null ? "" : rawUpdate.message().newChatMembers()[0].lastName();
      newUserName = fn + " " + ln;
      return;
    }

    if (rawUpdate.message() != null) {
      eventType = EventTypes.NEW_MESSAGE;
      setMessageVars(rawUpdate.message());
      msgDate = rawUpdate.message().date();
      return;
    }

    if (rawUpdate.editedMessage() != null) {
      eventType = EventTypes.EDITED_MESSAGE;
      setMessageVars(rawUpdate.editedMessage());
      rawUpdate.editedMessage().editDate();
      return;
    }

    if (rawUpdate.callbackQuery() != null) {
      eventType = EventTypes.CALLBACK_BUTTON;
      messageID = rawUpdate.callbackQuery().message().messageId();
      msgDate = rawUpdate.callbackQuery().message().date();
      senderId = rawUpdate.callbackQuery().from().id();
      callBackData = rawUpdate.callbackQuery().data();

    }


  }

  public EventTypes getEventType() {
    return eventType;
  }

  public String getUserName() {
    return userName;
  }

  public ChatTypes getChatType() {
    return chatType;
  }

  public int getMessageID() {
    return messageID;
  }

  public int getMessageThreadId() {
    return messageThreadId;
  }

  public int getMessageDate() {
    return msgDate;
  }

  public long getSenderId() {
    return senderId;
  }

  public long getChatId() {
    return chatId;
  }

  public String getMessageText() {
    return messageText;
  }

  public String getCaptionText() {
    return captionText;
  }

  public long getNewUserId() {
    return newUserId;
  }

  public String getNewUserName() {
    return newUserName;
  }

  public String getCallBackData() {
    return callBackData;
  }

  public Update getRawUpdate() {
    return rawUpdate;
  }

  private void setChatType() {
    Options options = Options.getInstance();
    if (rawUpdate.editedMessage() != null) {
      if (rawUpdate.editedMessage().chat().type().toString().equals("Private")) {
        chatType = ChatTypes.PRIVATE_CHAT;
      } else if (rawUpdate.editedMessage().chat().id().toString().equals(options.getAdminChatID())) {
        chatType = ChatTypes.ADMIN_CHAT;
      } else {
        chatType = ChatTypes.PUBLIC_CHAT;
      }
      return;
    }
    if (rawUpdate.message() != null) {
      if (rawUpdate.message().chat().type().toString().equals("Private")) {
        chatType = ChatTypes.PRIVATE_CHAT;
      } else if (rawUpdate.message().chat().id().toString().equals(options.getAdminChatID())) {
        chatType = ChatTypes.ADMIN_CHAT;
      } else {
        chatType = ChatTypes.PUBLIC_CHAT;
      }
      return;
    }
    chatType = ChatTypes.PUBLIC_CHAT;
  }

  private void setMessageVars(Message message) {
    messageID = message.messageId() == null ? 0 : message.messageId();
    messageThreadId = message.messageThreadId() == null ? 0 : message.messageThreadId();
    senderId = message.from().id() == null ? 0 : message.from().id();
    chatId = message.chat().id() == null ? 0 : message.chat().id();
    messageText = message.text() == null ? "" : message.text();
    captionText = message.caption() == null ? "" : message.caption();
    String fn = message.from().firstName() == null ? "" : message.from().firstName();
    String ln = message.from().lastName() == null ? "" : message.from().lastName();
    userName = fn + " " + ln;
  }
}
