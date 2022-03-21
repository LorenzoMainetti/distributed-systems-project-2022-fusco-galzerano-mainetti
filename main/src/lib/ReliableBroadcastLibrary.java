package lib;

import lib.message.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ReliableBroadcastLibrary extends Thread {
    private final MulticastSocket outSocket;
    private final DatagramSocket inSocket;

    private final int port;
    private final InetAddress address;

    private int sequenceNumber;

    private final List<InetAddress> view;
    private final Map<InetAddress, Integer> messageSeqMap;
    private final BlockingQueue<TextMessage> deliveredQueue;
    private final List<TextMessage> receivedList;
    private final Map<Integer, TextMessage> sentMessages;

    private BroadcastState state;

    public ReliableBroadcastLibrary(String inetAddr, int inPort) throws IOException {
        port = inPort;
        address = InetAddress.getByName(inetAddr);
        outSocket = new MulticastSocket(port);
        inSocket = new DatagramSocket();

        sequenceNumber = 0;
        messageSeqMap = new HashMap<>();
        receivedList = new LinkedList<>();
        sentMessages = new HashMap<>();
        deliveredQueue = new LinkedBlockingQueue<>();
        view = new ArrayList<>();

        state = BroadcastState.JOINING;

        this.start();
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
        } catch (IOException | InterruptedException e) {
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
        TextMessage textMessage = new TextMessage(address, text, sequenceNumber);
        sentMessages.put(sequenceNumber, textMessage);
        sendMessageHelper(textMessage);
        sequenceNumber++;
    }

    public void sendFlushMessage() {
        FlushMessage flushMessage = new FlushMessage(address);
        sendMessageHelper(flushMessage);
    }

    public TextMessage getTextMessage() throws InterruptedException {
        return deliveredQueue.take();
    }

    private void receiveMessage(Message m) {
        switch (m.getType()) {
            case 'T':
                TextMessage textMessage = (TextMessage) m;
                receivedList.add(textMessage);
                int expected = messageSeqMap.get(textMessage.getSource());
                if (textMessage.getSequenceNumber() > expected) {
                    //TODO: optimize
                    for (int i = expected; i < textMessage.getSequenceNumber(); ++i) {
                        sendNack(textMessage.getSource(), i);
                    }
                }
            case 'N':
                NackMessage nackMessage = (NackMessage) m;
                if (nackMessage.getTargetId() == address)
                    sendMessageHelper(sentMessages.get(nackMessage.getRequestedMessage()));
            case 'J':
                JoinMessage joinMessage = (JoinMessage) m;
                //messageSeqMap.put(joinMessage.getAddress(), joinMessage.getSequenceNumber());
                List<InetAddress> newView = new ArrayList<>(view);
                newView.add(joinMessage.getAddress());
                beginViewChange(newView);
            case 'V':
                ViewChangeMessage viewChangeMessage = (ViewChangeMessage) m;
                if (state != BroadcastState.VIEWCHANGE) {
                    beginViewChange(viewChangeMessage.getView());
                }
            default:

        }
    }

    private void beginViewChange(List<InetAddress> newView) {
        state = BroadcastState.VIEWCHANGE;
        // only flush logic, skip sending unstable messages for now
        sendFlushMessage();
        // wait to receive all flush from all other components of the view

        for (InetAddress address : newView) {

        }
    }

    private void sendNack(InetAddress targetAddress, int i) {
        NackMessage nackMessage = new NackMessage(address, targetAddress,  i);
        sendMessageHelper(nackMessage);
    }

    private void deliverAll() throws InterruptedException {
        boolean redo = true;
        while (redo) {
            redo = false;
            List<TextMessage> toRemove = new LinkedList<>();
            for (TextMessage m : receivedList) {
                int expected = messageSeqMap.get(m.getSource());
                if (m.getSequenceNumber() == expected) {
                    messageSeqMap.put(m.getSource(), expected + 1);
                    deliveredQueue.put(m);
                    redo = true;
                } else if (m.getSequenceNumber() < expected) {
                    toRemove.add(m);
                }
            }

            receivedList.removeAll(toRemove);
        }
    }
}

enum BroadcastState {
    NORMAL,
    VIEWCHANGE,
    JOINING
}
