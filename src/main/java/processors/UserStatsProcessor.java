package processors;

import com.pengrad.telegrambot.TelegramBot;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import processors.util.ChatTypes;
import processors.util.CommonDataHolder;
import processors.util.UpdateData;

public class UserStatsProcessor implements FeatureProcessor {
  private static final Logger logger = LogManager.getLogger(UserStatsProcessor.class);
  private CommonDataHolder commonData;


  public UserStatsProcessor(CommonDataHolder commonDataHolder) {
    this.commonData = commonDataHolder;

  }

  @Override
  public boolean processFeature(UpdateData update, TelegramBot bot) {
    if (skipProcessing(update)) {
      return true;
    }

    if (commonData.isUserKnown(update.getSenderId()) && update.getChatType() == ChatTypes.PUBLIC_CHAT) {
      commonData.addMessageCount(update.getSenderId());
      return true;
    }

    commonData.addUser(update.getSenderId(),
                       update.getRawUpdate().message().from().firstName(),
                       update.getRawUpdate().message().from().lastName(),
                       update.getRawUpdate().message().from().username(),
                       true);

    return true;
  }

  private boolean skipProcessing(UpdateData update) {

    return update.getRawUpdate().message() == null || update.getChatType() != ChatTypes.PUBLIC_CHAT;
  }


}
