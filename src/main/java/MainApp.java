import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import processors.util.Options;


public class MainApp {
  private static final Logger logger = LogManager.getLogger(MainApp.class);
  public static Options options = new Options();



  public static void main(String[] args) {
    logger.info("Starting Bot");
    Options options = Options.getInstance();
    try {
      options.loadOptions("default.ini");
      options.loadMessages("messages.ini");
      logger.info("Loaded options and messages.");
    } catch (Exception e) {
      logger.fatal("Could not load options. Error: {}", e.getMessage());
      System.exit(1);
    }
    BotHandler botHandler = new BotHandler(options.getBotToken());
    botHandler.run();


  }


}
