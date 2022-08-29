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
        try {
            while (true) {
                TextMessage m = library.getTextMessage();
                System.out.println("[MAIN] " + "{" + library.getAddress() + "} " + m.getMessage() + " FROM |" + m.getSource() + "| (" + library.getView() + ")");
            }
        } catch (InterruptedException e) {
            System.out.println("Disconnection occurred");
        }
    }
}
