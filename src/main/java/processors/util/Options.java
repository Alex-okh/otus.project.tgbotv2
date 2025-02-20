package processors.util;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Options {
  private static final Logger logger = LogManager.getLogger(Options.class);
  private static final Options INSTANCE = new Options();
  private OptionsData data;
  private Map<String, String> messages;


  public Options() {
    data = new OptionsData();
    data.botToken = "";
    data.adminChatID = "";
    data.botName = "pavlik_morozov_bot";
    data.dbPrefix = "jdbc:postgresql://";
    data.dbPath = "localhost";
    data.dbPort = "5432";
    data.dbUser = "";
    data.dbPassword = "";

    data.banThreshold = -10;
    data.shortestMessageToCheck = 20;
    data.checkExplicit = true;
    data.checkNewUsers = true;
    data.forwardMessages = true;
    data.saveUpdatesToFile = false;
    data.checkReplies = false;
    data.saveUpdatesToDB = true;
    data.savePrivateMessages = false;
    data.saveAdminMessages = false;
    data.savePublicMessages = true;
    data.checkChatHelper = false;

    data.explicitDictFile = "fuck.txt";
    data.chatHelperFile = "helper.txt";

    messages = new HashMap<>();
    messages.put("PRIVATE_HELLO",
                 "Привет, %s. Я - бот Павлик. Я могу переправить Ваше сообщение админам. Просто напишите мне, и я напишу ответ здесь сразу же, как кто-то из админов ответит.");
    messages.put("RULES",
                 """
                         Когда-нибудь (после бета-теста) тут будет много правил.
                         Пока правила два - нельзя материться в публичном чате и повторять сообщения чаще раза в неделю.
                         Не забывайте, что ручная модерация тоже есть.
                         Удачи.
                         """);
    messages.put("PRIVATE_HELP",
                 """
                         Все просто, %s!
                         Хочешь связаться с админами - отправь мне сообщение. Они его увидят. Ответ придет сюда.}
                         /rules - правила.
                         /help - это сообщение.
                          Больше я пока ничего не умею. :-)
                          """);
    messages.put("ADMIN_HELP",
                 """
                         Привет, %s!
                         В админском чате работают следующие команды:
                         -help - это сообщение.
                         -rules - показать правила.
                         -setrules [новые правила]- установить правила.
                         -set_admin_chat - установить текущий чат в качестве админки (!!! ВНИМАНИЕ. РАБОТАЕТ ТОЛЬКО ПРИ ОТСУТСТВИИ АДМИНКИ !!!
                         -clear_admin_chat - сбросить админку (для того, чтобы установить новую)
                         -set_ban_rating - установить границу бана. По умолчанию: -10. Рейтинг снижается на 1 за каждый мат и за каждое сообщение до авторизации.
                         -save_settings - сохранить текущие настройки (на случай перезапуска)
                          Больше я пока ничего не умею. :-)
                          """);

  }

  public static Options getInstance() {
    return INSTANCE;
  }

  public String getBotToken() {
    return data.botToken;
  }

  public String getBotName() {
    return data.botName;
  }

  public String getAdminChatID() {
    return data.adminChatID == null ? "" : data.adminChatID;
  }

  public void setAdminChatID(String adminChatID) {
    this.data.adminChatID = adminChatID;
  }

  public String getDbPath() {
    return data.dbPath;
  }

  public void setDbPath(String dbPath) {
    this.data.dbPath = dbPath;
  }

  public String getDbPort() {
    return data.dbPort;
  }

  public void setDbPort(String dbPort) {
    this.data.dbPort = dbPort;
  }

  public String getDbPrefix() {
    return data.dbPrefix;
  }

  public void setDbPrefix(String dbPrefix) {
    this.data.dbPrefix = dbPrefix;
  }

  public String getDbUser() {
    return data.dbUser;
  }

  public void setDbUser(String dbUser) {
    this.data.dbUser = dbUser;
  }

  public String getDbPassword() {
    return data.dbPassword;
  }

  public void setDbPassword(String dbPassword) {
    this.data.dbPassword = dbPassword;
  }

  public String getExplicitDictFile() {
    return data.explicitDictFile;
  }

  public void setExplicitDictFile(String explicitDictFile) {
    this.data.explicitDictFile = explicitDictFile;
  }

  public String getChatHelperFile() {
    return data.chatHelperFile;
  }

  public void setChatHelperFile(String chatHelperFile) {
    this.data.chatHelperFile = chatHelperFile;
  }

  public int getBanThreshold() {
    return data.banThreshold;
  }

  public void setBanThreshold(int banThreshold) {
    this.data.banThreshold = banThreshold;
  }

  public int getShortestMessageToCheck() {
    return data.shortestMessageToCheck;
  }

  public void setShortestMessageToCheck(int shortestMessageToCheck) {
    this.data.shortestMessageToCheck = shortestMessageToCheck;
  }

  public boolean isCheckExplicit() {
    return data.checkExplicit;
  }

  public void setCheckExplicit(boolean checkExplicit) {
    this.data.checkExplicit = checkExplicit;
  }

  public boolean isCheckNewUsers() {
    return data.checkNewUsers;
  }

  public void setCheckNewUsers(boolean checkNewUsers) {
    this.data.checkNewUsers = checkNewUsers;
  }

  public boolean isForwardMessages() {
    return data.forwardMessages;
  }

  public void setForwardMessages(boolean forwardMessages) {
    this.data.forwardMessages = forwardMessages;
  }

  public boolean isCheckChatHelper() {
    return data.checkChatHelper;
  }

  public void setCheckChatHelper(boolean checkChatHelper) {
    this.data.checkChatHelper = checkChatHelper;
  }

  public boolean isCheckReplies() {
    return data.checkReplies;
  }

  public void setCheckReplies(boolean checkReplies) {
    this.data.checkReplies = checkReplies;
  }

  public boolean isSaveUpdatesToFile() {
    return data.saveUpdatesToFile;
  }

  public void setSaveUpdatesToFile(boolean saveUpdatesToFile) {
    this.data.saveUpdatesToFile = saveUpdatesToFile;
  }

  public boolean isSaveUpdatesToDB() {
    return data.saveUpdatesToDB;
  }

  public void setSaveUpdatesToDB(boolean saveUpdatesToDB) {
    this.data.saveUpdatesToDB = saveUpdatesToDB;
  }

  public boolean isSavePrivateMessages() {
    return data.savePrivateMessages;
  }

  public void setSavePrivateMessages(boolean savePrivateMessages) {
    this.data.savePrivateMessages = savePrivateMessages;
  }

  public boolean isSaveAdminMessages() {
    return data.saveAdminMessages;
  }

  public void setSaveAdminMessages(boolean saveAdminMessages) {
    this.data.saveAdminMessages = saveAdminMessages;
  }

  public boolean isSavePublicMessages() {
    return data.savePublicMessages;
  }

  public void setSavePublicMessages(boolean savePublicMessages) {
    this.data.savePublicMessages = savePublicMessages;
  }

  public boolean saveOptions(String fileName) {
    Gson gson = new Gson();
    String json = gson.toJson(data);
    File file = new File(fileName);
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(file,
                                                               StandardCharsets.UTF_8,
                                                               false))) {
      bw.write(json);
      bw.flush();
      return true;
    } catch (IOException e) {
      logger.error(e);
      return false;
    }
  }

  public void saveMessages(String fileName) {
    Gson gson = new Gson();
    String json = gson.toJson(messages);
    File file = new File(fileName);
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(file,
                                                               StandardCharsets.UTF_8,
                                                               false))) {
      bw.write(json);
      bw.flush();
    } catch (IOException e) {
      logger.error(e);
    }
  }

  public void loadMessages(String fileName) {
    File file = new File(fileName);
    StringBuilder sb = new StringBuilder();
    String line = "";
    try (BufferedReader br = new BufferedReader(new FileReader(file,
                                                               StandardCharsets.UTF_8))) {
      while ((line = br.readLine()) != null) {
        sb.append(line);
      }
      String json = sb.toString();
      Gson gson = new Gson();
      this.messages = gson.fromJson(json,
                                    this.messages.getClass());

    } catch (IOException e) {
      logger.error(e);

    }
  }

  public void loadOptions(String fileName) {
    File file = new File(fileName);
    StringBuilder sb = new StringBuilder();
    String line = "";
    try (BufferedReader br = new BufferedReader(new FileReader(file,
                                                               StandardCharsets.UTF_8))) {
      while ((line = br.readLine()) != null) {
        sb.append(line);
      }
      String json = sb.toString();
      Gson gson = new Gson();
      this.data = gson.fromJson(json,
                                this.data.getClass());

    } catch (IOException e) {
      logger.fatal("Could not load options. Stopping. {}", e.getMessage());
      System.exit(1);
    }
  }

  public String getMessageText(String key) {
    String result = messages.getOrDefault(key,
                                          "");
    if (result.isEmpty()) {
      logger.warn("Empty message returned for key: {}",
                  key);
    }
    return result;
  }

  public void setMessageText(String key, String value) {
    messages.put(key,
                 value);
  }

  class OptionsData {
    private String botToken;
    private String botName;
    private String adminChatID;

    private String dbPath;
    private String dbPort;
    private String dbPrefix;
    private String dbUser;
    private String dbPassword;

    private String explicitDictFile;
    private String chatHelperFile;

    private int banThreshold;
    private int shortestMessageToCheck;

    private boolean checkExplicit;
    private boolean checkNewUsers;
    private boolean forwardMessages;
    private boolean checkChatHelper;
    private boolean checkReplies;
    private boolean saveUpdatesToFile;

    private boolean saveUpdatesToDB;
    private boolean savePrivateMessages;
    private boolean saveAdminMessages;
    private boolean savePublicMessages;
  }
}
