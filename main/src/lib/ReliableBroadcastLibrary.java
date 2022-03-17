package lib;

import lib.message.TextMessage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashMap;
import java.util.Map;

public class ReliableBroadcastLibrary {
    private MulticastSocket outSocket;
    private DatagramSocket inSocket;

    private int port;
    private InetAddress address;

    private int sequenceNumber;

    Map<InetAddress, Integer> messageSeqMap;

    public ReliableBroadcastLibrary(String inetAddr, int inPort) throws IOException {
        port = inPort;
        address = InetAddress.getByName(inetAddr);
        outSocket = new MulticastSocket(port);
        inSocket = new DatagramSocket();

        sequenceNumber = 0;
        messageSeqMap = new HashMap<>();
    }

    public void sendTextMessage(String text) throws IOException {
        TextMessage message = new TextMessage(text, address, sequenceNumber);
        byte[] transmissionBytes = message.getTransmissionString().getBytes();
        DatagramPacket packet = new DatagramPacket(transmissionBytes, transmissionBytes.length);
        outSocket.send(packet);
        sequenceNumber++;
    }
}
