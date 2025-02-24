import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.response.GetUpdatesResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import processors.util.MyDBConnection;
import tools.TimerTool;

import java.util.List;

public class BotHandler {
  private static final Logger logger = LogManager.getLogger(BotHandler.class);
  private final NewDispatcher dispatcher;
  private final TelegramBot bot;
  private int updatesOffcet;
  private boolean isRunning;
  private int connectionTimeout;


  public BotHandler(String botToken) {

    bot = new TelegramBot(botToken);
    dispatcher = new NewDispatcher(bot);
    updatesOffcet = 0;
    isRunning = true;
    connectionTimeout = 1;
    logger.info("Bot init completed.");
  }

  public void run() {
    long updateProcessingTime = 0;
    while (isRunning) {
      GetUpdates getUpdates = new GetUpdates().limit(100).offset(updatesOffcet).timeout(0);
      try {
        GetUpdatesResponse updatesResponse = bot.execute(getUpdates);
        List<Update> updates = updatesResponse.updates();
        connectionTimeout = 1;
        while (!updates.isEmpty()) {
          Update update = updates.remove(0);
          logger.info("Update processing started. UpdateID: {}",
                      update.updateId());
          TimerTool.stamp();
          try {
            dispatcher.processUpdate(update);
          } catch (Exception e) {
            logger.error("Could not process update {}. Dropping update. {} ",
                         update.updateId(),
                         e.getMessage());
          }

          updateProcessingTime = TimerTool.stop();
          logger.warn("Update {} processing completed. Processing time: {} ms.",
                      update.updateId(),
                      updateProcessingTime);
          updatesOffcet = update.updateId() + 1;
        }
        try {
          Thread.sleep(updateProcessingTime > 1000 ? 0 : 1000 - updateProcessingTime);
        } catch (InterruptedException ignored) {
        }

      } catch (Exception e) {
        logger.error("Unexpected error. Connection lost? {}",
                     e.getMessage());
        try {
          Thread.sleep(1000 * connectionTimeout);
          connectionTimeout = connectionTimeout < 200 ? connectionTimeout * 2 : connectionTimeout;
        } catch (InterruptedException ignored) {
        }
      }

    }
    logger.info("Bot stopped. Closing DB connection.");
    MyDBConnection db = MyDBConnection.getInstance();
    db.closeConnection();
  }


  public void stop() {
    isRunning = false;
  }

}


