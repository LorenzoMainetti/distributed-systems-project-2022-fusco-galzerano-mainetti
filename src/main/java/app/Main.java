package app;

import lib.client.ReliableBroadcastLibrary;
import lib.Settings;

import java.io.IOException;
import java.net.InetAddress;

public class Main {
    public static void main (String[] args) {
        long waitTime = (long)(Math.random() * Settings.SETUP_TIME);

        System.out.println("Hello world! I'm waiting for " + waitTime + " milliseconds");
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        try {

            System.out.println("localhost: " + InetAddress.getLocalHost());
            ReliableBroadcastLibrary lib = new ReliableBroadcastLibrary("224.0.0.1", 8888);

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
