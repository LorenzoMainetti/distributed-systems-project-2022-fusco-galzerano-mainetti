package app;

import lib.ReliableBroadcastLibrary;

import java.io.IOException;
import java.net.InetAddress;

public class Main {
    public static void main (String[] args) {
        System.out.println("Hello world!");
        try {
            ReliableBroadcastLibrary lib = new ReliableBroadcastLibrary("224.0.0.3", 8888);
            System.out.println(InetAddress.getLocalHost());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
