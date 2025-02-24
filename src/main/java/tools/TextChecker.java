package tools;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import processors.util.Options;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextChecker {
  private static final Logger logger = LogManager.getLogger(TextChecker.class);
  private static final Set<String> dictionary;
  private static final Map<String, String> helper;
  private static String dictionaryFile;
  private static String helperFile;

  static {
    Options options = Options.getInstance();
    dictionary = new HashSet<>();
    dictionaryFile = options.getExplicitDictFile();
    helper = new HashMap<>();
    helperFile = options.getChatHelperFile();
    loadDictionary();
    loadHelper();

  }

  private TextChecker() {
  }

  private static void loadDictionary() {
    File file = new File(dictionaryFile);
    logger.info("Loading bad words dictionary from {}", file.getPath());
    try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
      String line = br.readLine();
      while (line != null) {
        line = line.trim();
        dictionary.add(line.toLowerCase());
        line = br.readLine();
      }
    } catch (IOException e) {
      logger.error(e.getMessage());

    }
    logger.info("Loaded dictionary. {} words loaded.", dictionary.size());
  }

  public static void addToDictionary(String word) {
    dictionary.add(word.toLowerCase());
    File file = new File(dictionaryFile);
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8,true))) {
     bw.write("\n" + word.trim());
     logger.info("Saved {} to explicit dictionary. ", word);
    } catch (IOException e) {
      logger.error(e.getMessage());
    }

  }

  private static void loadHelper() {
    File file = new File(helperFile);
    logger.info("Loading helper from {}", file.getPath());
    try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
      String line = br.readLine();
      while (line != null) {
        String[] help = line.trim().split("=");
        helper.putIfAbsent(help[0].trim(), help[1].trim());
        line = br.readLine();
      }
    } catch (IOException e) {
      logger.error(e.getMessage());

    }
    logger.info("Loaded helper. {} pairs loaded.", helper.size());
  }

  public static void addHelper(String helperPhrase) {
    if (helperPhrase == null || !helperPhrase.contains("=")) {return;}
    String[] help = helperPhrase.trim().split("=", 2);
    helper.putIfAbsent(help[0].trim(), help[1].trim());
    File file = new File(helperFile);
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8,true))) {
      bw.write(helperPhrase);
      logger.info("Saved {} to helpers file. ", helperPhrase);
    } catch (IOException e) {
      logger.error(e.getMessage());
    }
  }


  public static boolean isTextOK(String checked) {
    if (checked == null) {
      return true;
    }
    Pattern regex = Pattern.compile("^.*(?iu)\\b((у|[нз]а|(хитро|не)?вз?[ыьъ]|с[ьъ]|(и|ра)[зс]ъ?|(о[тб]|под)[ьъ]?|(.\\B)+?[оаеи])?-?([её]б(?!о[рй])|и[пб][ае][тц]).*?|(н[иеа]|[дп]о|ра[зс]|з?а|с(ме)?|о(т|дно)?|апч)?-?ху([яйиеёю]|ли(?!ган)).*?|(в[зы]|(три|два|четыре)жды|(н|сук)а)?-?[б6]л(я(?!(х|ш[кн]|мб)[ауеыио]).*?|[еэ][дт]ь?)|(ра[сз]|[зн]а|[со]|вы?|п(р[ои]|од)|и[зс]ъ?|[ао]т)?п[иеё]зд.*?|(за)?п[ие]д[аое]?р((ас)?(и(ли)?[нщктл]ь?)?|(о(ч[еи])?)?к|юг)[ауеы]?|манд([ауеы]|ой|[ао]вошь?(е?к[ауе])?|юк(ов|[ауи])?)|муд([аио].*?|е?н([ьюия]|ей))|мля([тд]ь)?|лять|([нз]а|по)х|м[ао]л[ао]фь[яию])\\b.*$");
    Matcher matcher = regex.matcher(checked);
    if (matcher.find()) {
      logger.info("Bad word regex hit: {}", checked);
      return false;
    }

    checked = " " + checked.toLowerCase() + " ";
    checked = checked.replaceAll("\\p{P}", " ");
    checked = checked.replace("\n", " ");

    for (String word : dictionary) {
      if (checked.contains(" " + word + " ") || latToCyr(checked).contains(" " + word + " ")) {
        logger.info("Bad word dictionary hit ({}): {}", word, checked);
        return false;
      }
    }
    return true;
  }

  public static String helpFound(String checked) {
    if (checked == null || checked.isEmpty()) {
      return "";
    }

    checked = checked.toLowerCase();

    for (Map.Entry<String,String> entry : helper.entrySet()) {
      if (checked.contains(entry.getKey())) {
        return entry.getValue();
      }
    }
    return "";
  }

  public static String latToCyr(String input) {
    Map<Character, Character> replaceMap = new HashMap<>();
    replaceMap.put('A', 'А');
    replaceMap.put('B', 'В');
    replaceMap.put('C', 'С');
    replaceMap.put('E', 'Е');
    replaceMap.put('H', 'Н');
    replaceMap.put('K', 'K');
    replaceMap.put('M', 'М');
    replaceMap.put('O', 'О');
    replaceMap.put('P', 'Р');
    replaceMap.put('T', 'Т');
    replaceMap.put('X', 'Х');
    replaceMap.put('Y', 'У');
    replaceMap.put('a', 'а');
    replaceMap.put('c', 'с');
    replaceMap.put('e', 'е');
    replaceMap.put('g', 'д');
    replaceMap.put('k', 'к');
    replaceMap.put('m', 'т');
    replaceMap.put('n', 'п');
    replaceMap.put('o', 'о');
    replaceMap.put('p', 'р');
    replaceMap.put('r', 'г');
    replaceMap.put('u', 'и');
    replaceMap.put('x', 'х');
    replaceMap.put('y', 'у');


    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < input.length(); i++) {
      Character ch = replaceMap.getOrDefault(input.charAt(i), input.charAt(i));
      sb.append(ch);
    }
    return sb.toString();
  }


}
