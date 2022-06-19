package lib;

public class Utils {
    public static void sleepRandomRange(long maxMillis) {
        long millis = (long)(Math.random() * maxMillis);
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
