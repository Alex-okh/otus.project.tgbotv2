package tools;

public abstract class TimerTool {
  private static long timestamp;

  public static void stamp() {
    timestamp = System.nanoTime();
  }

  public static long stop() {
    return (System.nanoTime() - timestamp)/1000000;
  }
}