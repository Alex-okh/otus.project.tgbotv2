package processors;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import com.pengrad.telegrambot.request.BanChatMember;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import processors.util.*;


public class UserConfirmationProcessor implements FeatureProcessor {
  private static final Logger logger = LogManager.getLogger(UserConfirmationProcessor.class);
  private CommonDataHolder commonData;

  public UserConfirmationProcessor(CommonDataHolder commonData) {
    this.commonData = commonData;
  }

  @Override
  public boolean processFeature(UpdateData update, TelegramBot bot) {
    Options options = Options.getInstance();
    if (skipProcessing(update)) {
      return true;
    }

    switch (update.getEventType()) {
      case NEW_MESSAGE -> {
        if (commonData.isUserKnown(update.getSenderId()) && !commonData.isUserConfirmed(update.getSenderId())) {
          DeleteMessage dm = new DeleteMessage(update.getChatId(),
                                               update.getMessageID());
          bot.execute(dm);
          int userRating = commonData.decUserRating(update.getSenderId());
          if (userRating < options.getBanThreshold()) {
            BanChatMember bm = new BanChatMember(update.getChatId(),
                                                 update.getSenderId());
            BaseResponse banResponse = bot.execute(bm);
            if (banResponse.isOk()) {
              logger.info("Unconfirmed user {} banned successfully.",
                          update.getSenderId());
            } else {
              logger.warn("Tried to ban unconfirmed user {}, but failed. Reason: {}",
                          update.getSenderId(),
                          banResponse.description());
            }
          }
        }
      }

      case CALLBACK_BUTTON -> {
        String quieryID = update.getRawUpdate().callbackQuery().id();
        String[] splitted = update.getCallBackData().split(":");
        long callbackUserID = Long.parseLong(splitted[0]);
        long callbackChatID = Long.parseLong(splitted[1]);

        if (update.getSenderId() == callbackUserID) {
          DeleteMessage dm = new DeleteMessage(callbackChatID,
                                               update.getMessageID());
          bot.execute(dm);
          AnswerCallbackQuery acbq = new AnswerCallbackQuery(quieryID).text("Принято!").showAlert(true);
          bot.execute(acbq);
          SendMessage sm = new SendMessage(callbackChatID,
                                           "Ну ОК. Добро пожаловать в чат. Просьба ознакомиться с правилами и не нарушать их, вести себя прилично. За правилами вэлкам ко мне в личку");
          bot.execute(sm);
          commonData.approveUser(callbackUserID);
          return false;
        } else {
          AnswerCallbackQuery acbq = new AnswerCallbackQuery(quieryID).text("Это чужая кнопка!").showAlert(true);
          bot.execute(acbq);
        }
      }

      case NEW_CHAT_MEMBER -> {
        if (commonData.isUserConfirmed(update.getNewUserId())) {
          sayHello(update.getChatId(),
                   update.getNewUserName(),
                   bot);
          return true;
        }
        if (update.getSenderId() != update.getNewUserId()) {
          logger.info("New user id {} added by existing one. Passing by checking...",
                      update.getNewUserId());
          commonData.addUser(update.getNewUserId(),
                             update.getRawUpdate().message().newChatMembers()[0].firstName(),
                             update.getRawUpdate().message().newChatMembers()[0].lastName(),
                             update.getRawUpdate().message().newChatMembers()[0].username(),
                             true);

          sayHello(update.getChatId(),
                   update.getNewUserName(),
                   bot);

        } else {
          logger.info("New user id {} entered by a link.",
                      update.getNewUserId());
          commonData.addUser(update.getNewUserId(),
                             update.getRawUpdate().message().newChatMembers()[0].firstName(),
                             update.getRawUpdate().message().newChatMembers()[0].lastName(),
                             update.getRawUpdate().message().newChatMembers()[0].username(),
                             false);

          InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup(new InlineKeyboardButton("Да, я не бот!").callbackData(
                  update.getNewUserId() + ":" + update.getChatId()),
                                                                         new InlineKeyboardButton("Нет, я не бот!").callbackData(
                                                                                 update.getNewUserId() + ":" +
                                                                                 update.getChatId()));

          SendMessage sm = new SendMessage(update.getChatId(),
                                           "Привет, " + update.getNewUserName() +
                                           "! Скажи, а ты не бот? \n Если не бот - нажми кнопку внизу.");
          sm.replyMarkup(inlineKeyboard);
          bot.execute(sm);
        }
      }
    }
    return true;


  }


  private boolean skipProcessing(UpdateData update) {

    if (update.getChatType() == ChatTypes.ADMIN_CHAT || update.getChatType() == ChatTypes.PRIVATE_CHAT) {
      return true;
    }
    if (update.getEventType() == EventTypes.NEW_CHAT_MEMBER || update.getEventType() == EventTypes.CALLBACK_BUTTON ||
        update.getEventType() == EventTypes.NEW_MESSAGE) {
      return false;
    }


    return true;
  }

  private void sayHello(long chatID, String name, TelegramBot bot) {
    String HELLO_MESSAGE = "Привет, " + name + "!" +
                           "\n Я - бот Павлик. Напиши мне в личку, чтобы прочитать правила или если нужна связь с админами.";
    SendMessage sm = new SendMessage(chatID,
                                     HELLO_MESSAGE);
    BaseResponse response = bot.execute(sm);
    if (!response.isOk()) {
      logger.warn("Hello to new user was not OK. Reason: {}",
                  response.description());
    }
  }
}


