package lib;

import lib.message.Message;
import lib.message.NackMessage;
import lib.message.TextMessage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.time.temporal.TemporalQuery;
import java.util.*;

public class ReliableBroadcastLibrary extends Thread {
    private MulticastSocket outSocket;
    private DatagramSocket inSocket;

    private int port;
    private InetAddress address;

    private int sequenceNumber;

    Map<InetAddress, Integer> messageSeqMap;
    private Queue<TextMessage> deliveredQueue;
    private List<TextMessage> receivedList;
    private List<TextMessage> sentMessages;

    public ReliableBroadcastLibrary(String inetAddr, int inPort) throws IOException {
        port = inPort;
        address = InetAddress.getByName(inetAddr);
        outSocket = new MulticastSocket(port);
        inSocket = new DatagramSocket();

        sequenceNumber = 0;
        messageSeqMap = new HashMap<>();
        receivedList = new LinkedList<>();
        messageSeqMap = new HashMap<>();
        sentMessages = new ArrayList<>();

        this.run();
    }

    public void run() {
        try {
            while (true) {
                byte[] in = new byte[2048];
                DatagramPacket packet = new DatagramPacket(in, in.length);
                inSocket.receive(packet);
                Message m = Message.parseString(new String(packet.getData()), packet.getAddress());

                receiveMessage(m);
                deliverAll();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessageHelper(Message m) {
        byte[] buf = m.getTransmissionString().getBytes();
        try {
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
            outSocket.send(packet);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendTextMessage(String text) {
        TextMessage textMessage = new TextMessage(text, address, sequenceNumber);
        sentMessages.set(sequenceNumber, textMessage);
        sendMessageHelper(textMessage);
        sequenceNumber++;
    }

    public TextMessage getTextMessage() throws InterruptedException {
        TextMessage textMessage;
        synchronized (deliveredQueue) {
            while (deliveredQueue.isEmpty()) deliveredQueue.wait();
            textMessage = deliveredQueue.remove();
        }

        return textMessage;
    }

    private void receiveMessage(Message m) {
        switch (m.getType()) {
            case 'T':
                TextMessage textMessage = (TextMessage) m;
                receivedList.add(textMessage);
                int expected = messageSeqMap.get(textMessage.getSenderId());
                if (textMessage.getSequenceNumber() > expected) {
                    //TODO: optimize
                    for (int i = expected; i < textMessage.getSequenceNumber(); ++i) {
                        sendNack(textMessage.getSenderId(), i);
                    }
                }
            case 'N':
                NackMessage nackMessage = (NackMessage) m;
                if (nackMessage.getTargetId() == address)
                    sendMessageHelper(sentMessages.get(nackMessage.getRequestedMessage()));
            default:

        }
    }

    private void sendNack(InetAddress targetAddress, int i) {
        NackMessage nackMessage = new NackMessage(address, targetAddress,  i);
        sendMessageHelper(nackMessage);
    }

    private void deliverAll() {
        boolean redo = true;
        while (redo) {
            redo = false;
            List<TextMessage> toRemove = new LinkedList<>();
            for (TextMessage m : receivedList) {
                int expected = messageSeqMap.get(m.getSenderId());
                if (m.getSequenceNumber() == expected) {
                    messageSeqMap.put(m.getSenderId(), expected + 1);
                    deliveredQueue.add(m);
                    redo = true;
                } else if (m.getSequenceNumber() < expected) {
                    toRemove.add(m);
                }
            }

            receivedList.removeAll(toRemove);
        }
    }
}
