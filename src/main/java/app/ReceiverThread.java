package app;

import lib.client.ReliableBroadcastLibrary;
import lib.client.Settings;
import lib.message.TextMessage;

import java.io.IOException;
import java.net.InetAddress;

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
                System.out.println("[MAIN] " + m.getMessage() + " FROM " + m.getSource());
            }
        } catch (InterruptedException e) {
            System.out.println("Disconnection occurred");
        }
    }
}
