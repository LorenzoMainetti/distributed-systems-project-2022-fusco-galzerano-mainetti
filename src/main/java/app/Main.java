package app;

import lib.ReliableBroadcastLibrary;
import lib.message.TextMessage;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Scanner;

public class Main {
    public static void main (String[] args) {
        long waitTime = (long)(Math.random() * 10000);
        int numSend = (int)(Math.random() * 31) + 20;
        int i = 0;

        System.out.println("Hello world! I'm waiting for " + waitTime + " milliseconds");
        try {
            Thread.sleep(waitTime);
            ReliableBroadcastLibrary lib = new ReliableBroadcastLibrary("224.0.0.1", 8888);
            System.out.println("localhost: " + InetAddress.getLocalHost());
            //Scanner scanner = new Scanner(System.in);
            //String msg = scanner.nextLine();
            while (i<numSend) {
                lib.sendTextMessage("hello");
                i++;
                Thread.sleep(50000);
            }
            TextMessage msg = lib.getTextMessage();
            System.out.println("Last delivered message is: " + msg.getMessage() + " from " + msg.getSource().getCanonicalHostName());

            lib.leaveGroup();
            System.exit(0);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
