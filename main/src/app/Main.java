package app;

import lib.ReliableBroadcastLibrary;

import java.io.IOException;

public class Main {
    public static void main (String[] args) {
        System.out.println("Hello world!");
        try {
            ReliableBroadcastLibrary lib = new ReliableBroadcastLibrary("224.0.0.3", 8888);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
