package app;

import lib.client.ReliableBroadcastLibrary;
import lib.Settings;
import java.io.IOException;

public class SenderThread extends Thread {
    private final ReliableBroadcastLibrary library;

    public SenderThread(ReliableBroadcastLibrary library) {
        this.library = library;
    }

    @Override
    public void run(){
        int i = 0, j = (int)(Math.random() * 10) + 10;
        System.out.println("[INFO] will leave after " + j + " messages");
        try {
            while (true) {
                library.sendTextMessage(String.valueOf(i++));
                Thread.sleep(Settings.T_FREQUENCY);

                // Countdown for leaving client simulation
                //System.out.println("Check ->" + j);
                if (i >= j) {
                    System.out.println("START LEAVING");
                    library.leaveGroup();
                    System.exit(0);
                }
            }
        } catch (InterruptedException | IOException e) {
            System.out.println("Disconnection occurred");
        }
    }
}
