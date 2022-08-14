package app;

import lib.client.ReliableBroadcastLibrary;
import lib.message.TextMessage;
import java.io.IOException;

public class ReceiverThread extends Thread {
    private final ReliableBroadcastLibrary library;

    public ReceiverThread(ReliableBroadcastLibrary library) {
        this.library = library;
    }

    @Override
    public void run() {
        int j=(int)(Math.random() * 20);
        try {
            while (true) {
                TextMessage m = library.getTextMessage();
                System.out.println("[MAIN] " + m.getMessage() + " FROM " + m.getSource());

                // Countdown for leaving client simulation
                //System.out.println("Check ->" + j);
                if (j == 0) {
                    System.out.println("START LEAVING");
                    library.leaveGroup();
                    System.exit(0);
                }else {
                    j--;
                }
            }
        } catch (InterruptedException | IOException e) {
            System.out.println("Disconnection occurred");
        }
    }
}
