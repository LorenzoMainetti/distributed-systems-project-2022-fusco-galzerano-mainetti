package app;

import lib.client.ReliableBroadcastLibrary;
import lib.client.Settings;
import lib.message.TextMessage;

import java.io.IOException;
import java.net.InetAddress;

public class SenderThread extends Thread {
    private final ReliableBroadcastLibrary library;

    public SenderThread(ReliableBroadcastLibrary library) {
        this.library = library;
    }

    @Override
    public void run() {
        try {
            while (true) {
                synchronized (library) {
                    library.sendTextMessage("hello");
                }
                Thread.sleep(Settings.T_FREQUENCY);
            }
        } catch (InterruptedException e) {
            System.out.println("Disconnection occurred");
        }
    }
}
