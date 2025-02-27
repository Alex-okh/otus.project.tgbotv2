package processors;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.response.BaseResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import processors.util.CommonDataHolder;

import java.util.Date;

public class DelayedDeleteProcessor implements Runnable {
  private static final Logger logger = LogManager.getLogger(DelayedDeleteProcessor.class);
  private TelegramBot bot;
  private CommonDataHolder commonData;

  public DelayedDeleteProcessor(TelegramBot bot, CommonDataHolder commonDataHolder) {
    commonData = commonDataHolder;
    this.bot = bot;
  }


  public void run() {
    while (true) {
      Date nextTime = commonData.getNextDeletedTime();
      if (nextTime == null) {
        try {
          Thread.sleep(10000);
        } catch (InterruptedException ignored) {
        }
      } else if (nextTime.after(new Date())) {
        try {
          Thread.sleep(nextTime.getTime() - (new Date().getTime()));
        } catch (InterruptedException ignored) {
        }

      } else {
        DeleteMessage dm = commonData.getDelayedMessage();
        BaseResponse br = bot.execute(dm);
        if (br.isOk()) {
          logger.info("Delayed message deleted");
        } else {
          logger.error("Delayed message delete failed! Reason: {} ",
                       br.description());
        }

      }
    }
  }


}
