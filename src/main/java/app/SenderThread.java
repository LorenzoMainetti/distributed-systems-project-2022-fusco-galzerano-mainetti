package app;

import lib.client.ReliableBroadcastLibrary;
import lib.Settings;

public class SenderThread extends Thread {
    private final ReliableBroadcastLibrary library;

    public SenderThread(ReliableBroadcastLibrary library) {
        this.library = library;
    }

    @Override
    public void run() {
        int i = 0;
        try {
            while (true) {
                synchronized (library) {
                    library.sendTextMessage(String.valueOf(i++));
                }
                Thread.sleep(Settings.T_FREQUENCY);
            }
        } catch (InterruptedException e) {
            System.out.println("Disconnection occurred");
        }
    }
}
