package processors;

import com.pengrad.telegrambot.TelegramBot;
import processors.util.UpdateData;


public interface FeatureProcessor {
  boolean processFeature(UpdateData update, TelegramBot bot);

}
