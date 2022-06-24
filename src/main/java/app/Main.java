package app;

import lib.ReliableBroadcastLibrary;
import lib.Settings;
import lib.message.TextMessage;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Scanner;

public class Main {
    public static void main (String[] args) {
        long waitTime = (long)(Math.random() * Settings.SETUP_TIME);
        int numSend = (int)(Math.random() * 31) + 20;
        int i = 0;

        System.out.println("Hello world! I'm waiting for " + waitTime + " milliseconds");
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        while (true) {
            try {
                ReliableBroadcastLibrary lib = new ReliableBroadcastLibrary("224.0.0.1", 8888);
                System.out.println("localhost: " + InetAddress.getLocalHost());
                while (i<numSend) {
                    lib.sendTextMessage("hello");
                    i++;
                    Thread.sleep(Settings.T_FREQUENCY);
                }
                TextMessage msg = lib.getTextMessage();
                System.out.println("Last delivered message is: " + msg.getMessage() + " from " + msg.getSource().getCanonicalHostName());

                lib.leaveGroup();
                //System.exit(0);

            } catch (IOException | InterruptedException e) {
                System.out.println("Disconnection occurred");
            }
        }

    }
}
