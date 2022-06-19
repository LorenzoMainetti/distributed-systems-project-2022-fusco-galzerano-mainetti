package app;

import lib.ReliableBroadcastLibrary;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Scanner;

public class Main {
    public static void main (String[] args) {
        long waitTime = (long)(Math.random() * 10000);
        System.out.println("Hello world! I'm waiting for " + waitTime + " milliseconds");
        try {
            Thread.sleep(waitTime);
            ReliableBroadcastLibrary lib = new ReliableBroadcastLibrary("224.0.0.1", 8888);
            System.out.println("localhost: " + InetAddress.getLocalHost());
            //Scanner scanner = new Scanner(System.in);
            //String msg = scanner.nextLine();
            while (true) {
                lib.sendTextMessage("hello");
                Thread.sleep(50000);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
