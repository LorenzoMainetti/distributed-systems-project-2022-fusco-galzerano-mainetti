package app;

import lib.Test;
import lib.client.ReliableBroadcastLibrary;
import lib.Settings;

import java.io.IOException;
import java.net.InetAddress;

public class Main {
    public static void main (String[] args) {

        Test.DROP_TEXT_MESSAGE_RATIO = 0.1;
        Test.UNORDERED_CHANCE = 0.9;
        Test.UNORDERED = true;

        try {

            ReliableBroadcastLibrary lib = new ReliableBroadcastLibrary("224.0.0.1", 8888);
            System.out.println("Hello world! I am node-" + lib.getId() + " and I am waiting for " + Settings.INITIAL_TIMEOUTS.get(lib.getId()) + " milliseconds");
            Thread.sleep(Settings.INITIAL_TIMEOUTS.get(lib.getId()));
            System.out.println("localhost: " + InetAddress.getLocalHost() + "; id: " + lib.getId());

            ReceiverThread receiverThread = new ReceiverThread(lib);
            SenderThread senderThread = new SenderThread(lib);

            receiverThread.start();
            senderThread.start();

            senderThread.join();
            receiverThread.join();

        } catch (IOException | InterruptedException e) {
            System.out.println("Disconnection occurred");
        }

    }
}
