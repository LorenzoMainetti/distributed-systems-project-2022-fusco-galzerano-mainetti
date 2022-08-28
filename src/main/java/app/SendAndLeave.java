package app;

import lib.Settings;
import lib.Test;
import lib.client.ReliableBroadcastLibrary;

import java.io.IOException;
import java.net.InetAddress;

public class SendAndLeave {

    public static void main(String[] args) {

        Test.DROP_TEXT_MESSAGE_RATIO = 0;
        Test.UNORDERED = false;

        try {
            ReliableBroadcastLibrary lib = new ReliableBroadcastLibrary("224.0.0.1", 8888);
            System.out.println("Hello world! I am node-" + ReliableBroadcastLibrary.getId());
            System.out.println("localhost: " + InetAddress.getLocalHost() + "; id: " + ReliableBroadcastLibrary.getId());

            Thread.sleep(10000);

            if (ReliableBroadcastLibrary.getId() == 1) {
                lib.sendTextMessage("hello there!");
            }

            Thread.sleep(5000);

            if (ReliableBroadcastLibrary.getId() == 2) {
                Test.dropNextMessage = true;
            }

            if (ReliableBroadcastLibrary.getId() == 1) {
                lib.sendTextMessage("should not deliver!");
            }

            Thread.sleep(5000);

            Thread.sleep(ReliableBroadcastLibrary.getId() * 5000);

            lib.leaveGroup();

        } catch (IOException | InterruptedException e) {
            System.out.println("Disconnection occurred");
        }
    }
}
