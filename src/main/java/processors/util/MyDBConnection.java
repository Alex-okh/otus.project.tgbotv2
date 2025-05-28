package processors.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

public class MyDBConnection {
  private static final Logger logger = LogManager.getLogger(MyDBConnection.class);
  private static MyDBConnection INSTANCE = new MyDBConnection();
  public final Connection dbconnection;
  private final Options options;


  private MyDBConnection() {
    options = Options.getInstance();
    Connection newDBconnection = null;
    final String createDB_QUERY = """
          	CREATE TABLE public.repeats (
          	id serial4 NOT NULL,
          	user_id int8 NOT NULL,
          	chat_id int8 NOT NULL,
          	msg_id int4 NOT NULL,
          	msg_time timestamp NOT NULL,
          	msg_text varchar NULL,
          	caption varchar NULL,
          	CONSTRAINT repeats_pk PRIMARY KEY (id)
          );
          CREATE TABLE public.updatedump (
          	updateid int4 NOT NULL,
          	"update" varchar NULL,
          	CONSTRAINT updatedump_pk PRIMARY KEY (updateid)
          );
          CREATE TABLE public.user_data (
          	user_id int8 NOT NULL,
          	firstname varchar NULL,
          	lastname varchar NULL,
          	username varchar NULL,
          	rating int4 NULL,
          	isconfirmed bool NULL,
          	totalmessages int4 NULL,
          	CONSTRAINT user_data_pk PRIMARY KEY (user_id)
          );
          """;
    try {
      newDBconnection = DriverManager.getConnection(options.getDbPrefix()+options.getDbPath()+":"+options.getDbPort()+"/bot_db", options.getDbUser(), options.getDbPassword());
      logger.info("Connected to database...");
    } catch (SQLException ex) {
      logger.error("Could not connect to database, Errorcode : {}", ex.toString());

    }
    dbconnection = newDBconnection;
    if (isDBExist()) {
      logger.info("Existing bot database found...");

    } else {
      logger.info("Database not found, creating new one...");
      try (PreparedStatement prs = dbconnection.prepareStatement(createDB_QUERY)) {
        prs.execute();
        logger.info("Database created");
      } catch (SQLException ex) {
        logger.error("Could not create database, Errorcode : {}", ex.toString());

      }
    }

  }

  public static MyDBConnection getInstance() {
    boolean isValid = false;
    try { isValid = INSTANCE.dbconnection.isValid(5); }
    catch (SQLException ignored) {
      logger.error("Could not connect to database, Errorcode : {}", ignored.toString());
    }

    if (INSTANCE == null || INSTANCE.dbconnection == null || !isValid) {
      INSTANCE = new MyDBConnection();}
    return INSTANCE;
  }

  private boolean isDBExist() {
    try (PreparedStatement prs = dbconnection.prepareStatement("""
                  SELECT  to_regclass('repeats') IS NOT null and
                          to_regclass('updatedump') IS NOT null and
                          to_regclass('user_data') IS NOT null AS table_exists;
                  """)) {
            ResultSet rs = prs.executeQuery();
            if (rs.next() &&  rs.getBoolean(1)) {
              return true;
            }
    } catch (SQLException e) {
      logger.error("SQLException occurred while checking if DB exists!");
    } catch (NullPointerException ex) {
      logger.error("NullPointerException occurred while checking if DB exists!");
    }

return false;
  }

  public void closeConnection(){
    try {dbconnection.close();
  } catch (SQLException ex) {
    logger.error("SQLException occurred while closing connection! {}", ex.getMessage());}
  }
}
