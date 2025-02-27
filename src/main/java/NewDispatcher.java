import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import processors.*;
import processors.util.CommonDataHolder;
import processors.util.UpdateData;

import java.util.ArrayList;

public class NewDispatcher {
  private static final Logger logger = LogManager.getLogger(NewDispatcher.class);
  private ArrayList<FeatureProcessor> processors;
  private TelegramBot bot;

  private FeatureProcessor saveUpdateProcessor;
  private FeatureProcessor userStatsProcessor;
  private FeatureProcessor checkExplicitProcessor;
  private FeatureProcessor forwardMessagesProcessor;
  private FeatureProcessor userConfirmationProcessor;
  private FeatureProcessor adminCommandProcessor;
  private FeatureProcessor publicCommandProcessor;
  private FeatureProcessor personalCommandProcessor;
  private FeatureProcessor publicHelperProcessor;
  private FeatureProcessor checkRepeatsProcessor;
  private DelayedDeleteProcessor delayedDeleteProcessor;

  public NewDispatcher(TelegramBot bot) {
    this.bot = bot;
    CommonDataHolder commonData = new CommonDataHolder();

    processors = new ArrayList<>();
    saveUpdateProcessor = new SaveUpdateProcessor();
    userStatsProcessor = new UserStatsProcessor(commonData);
    checkExplicitProcessor = new CheckExplicitProcessor(commonData);
    forwardMessagesProcessor = new ForwardMessageProcessor(commonData);
    userConfirmationProcessor = new UserConfirmationProcessor(commonData);
    personalCommandProcessor = new PersonalCommandProcessor(commonData);
    publicHelperProcessor = new PublicHelperProcessor(commonData);
    checkRepeatsProcessor = new CheckRepeatsProcessor(commonData);
    adminCommandProcessor = new AdminCommandProcessor(commonData);
    delayedDeleteProcessor = new DelayedDeleteProcessor(bot, commonData);

    processors.add(checkRepeatsProcessor);
    processors.add(checkExplicitProcessor);
    processors.add(userConfirmationProcessor);
    processors.add(userStatsProcessor);
    processors.add(saveUpdateProcessor);
    processors.add(forwardMessagesProcessor);
    processors.add(personalCommandProcessor);
    processors.add(publicHelperProcessor);
    processors.add(adminCommandProcessor);

    Thread delayThread = new Thread(delayedDeleteProcessor);
    delayThread.start();
  }

  public void processUpdate(Update update) {
    UpdateData updateData = new UpdateData(update);

    for (FeatureProcessor processor : processors) {
      if (!processor.processFeature(updateData, bot)) {
        break;
      }
    }
  }
}



