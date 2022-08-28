package lib.client;

import lib.Test;
import lib.utils.Pair;
import lib.MessageReceiver;
import lib.Receiver;
import lib.Settings;
import lib.client.state.ClientState;
import lib.client.state.DisconnectedState;
import lib.client.state.JoiningState;
import lib.message.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ReliableBroadcastLibrary implements Receiver {
    private final MulticastSocket ioSocket;

    private final int port;
    private final InetAddress targetAddress;
    private final InetAddress myAddress;

    private int sequenceNumber;

    private List<InetAddress> view;
    private List<InetAddress> pendingView;
    private final Map<InetAddress, Integer> messageSeqMap;
    private final BlockingQueue<TextMessage> deliveredQueue;
    private final List<TextMessage> receivedList;
    private final Queue<TextMessage> toSend;
    private final Map<Integer, TextMessage> sentUnstableMessages;
    private final Map<InetAddress, Map<Integer, Pair<List<InetAddress>, TextMessage>>> receivedUnstableMessages;

    private ClientState state;
    private List<InetAddress> partialViewFlushAwaitList;

    private final MessageReceiver messageReceiver;

    /**
     * Constructor
     * @param inetAddr is the address of the process
     * @param inPort is the input port for the process
     * @throws IOException
     */
    public ReliableBroadcastLibrary(String inetAddr, int inPort) throws IOException {
        port = inPort;
        targetAddress = InetAddress.getByName(inetAddr);
        myAddress = InetAddress.getLocalHost();
        ioSocket = new MulticastSocket(port);
        //ioSocket.setLoopbackMode(true);  //disable multicast loopback (receive own messages)
        ioSocket.joinGroup(targetAddress);

        sequenceNumber = 0;
        messageSeqMap = new HashMap<>();
        receivedList = new LinkedList<>();
        sentUnstableMessages = new HashMap<>();
        receivedUnstableMessages = new HashMap<>();
        deliveredQueue = new LinkedBlockingQueue<>();
        toSend = new LinkedList<>();
        view = new ArrayList<>();
        view.add(myAddress);

        state = new JoiningState(this);

        messageReceiver = new MessageReceiver(this);
        messageReceiver.start();
    }

    public int getId() {
        return Integer.parseInt(System.getenv("NODE"));
    }

    public List<InetAddress> getView() {
        return view;
    }

    public Map<InetAddress, Map<Integer, Pair<List<InetAddress>, TextMessage>>> getReceivedUnstableMessages() {
        return receivedUnstableMessages;
    }

    public List<TextMessage> getUnstableMessages() {
        return new ArrayList<>(sentUnstableMessages.values());
    }

    /**
     * This function creates a datagramPacket to be received through the {@Link ioSocket}
     */
    public Message receiveMessage() throws IOException {
        DatagramPacket packet;
        do {
            byte[] in = new byte[2048];
            packet = new DatagramPacket(in, in.length);
            ioSocket.receive(packet);
        } while (myAddress.equals(packet.getAddress()));

        return Message.parseString(new String(packet.getData()), packet.getAddress());
    }

    /**
     * This function creates a datagramPacket to be sent through the {@Link outSocket}
     * @param m message to be sent
     */
    public void sendMessageHelper(Message m) {
        byte[] buf = m.getTransmissionString().getBytes();
        try {
            if (m.getType() != 'P' && m.getType() != 'J')
                System.out.println("[SEND] " + m.getTransmissionString());
            DatagramPacket packet = new DatagramPacket(buf, buf.length, targetAddress, port);
            ioSocket.send(packet);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addToSend(TextMessage m) {
        toSend.add(m);
    }

    public void addUnstableMessage(TextMessage m) {
        sentUnstableMessages.put(m.getSequenceNumber(), m);
    }

    /**
     * API call to retrieve the last delivered message:
     * @return the last message in the queue of delivered messages
     * @throws InterruptedException InterruptedException
     */
    public TextMessage getTextMessage() throws InterruptedException {
        return deliveredQueue.take();
    }

    /**
     * API call to leave the group and close the communication:
     * This function is used to inform the other processes in the group that the process is leaving through a message
     * and then exit
     */
    public void leaveGroup() throws IOException {
        System.out.println("[DISCONNECT]");
        sendMessageHelper(new LeaveMessage(targetAddress, sequenceNumber));
        state.close();
        messageReceiver.close();
        state = new DisconnectedState(this);
        //TODO System.exit(0) is done by the app
    }

    /**
     * This function is used to receive messages and do different operations depending on the type of message received
     * @param m is the received message that has to be processed
     */
    public void putMessage(Message m) throws InterruptedException, IOException {
        if (m.getType() != 'P' && m.getType() != 'E' && m.getType() != 'J' && m.getType() != 'A')
            System.out.println("[" + state.getClass().getSimpleName() + "] processing " + m.getType() + " message from " + m.getSource());
        switch (m.getType()) {
            case 'T':
                if(view.contains(m.getSource())) {
                    TextMessage textMessage = (TextMessage) m;
                    if (Math.random() < Test.DROP_TEXT_MESSAGE_RATIO || Test.dropNextMessage) {
                            Test.dropNextMessage = false;
                            System.out.println("[DROP] SIMULATING MESSAGE NOT RECEIVED (" + textMessage.getMessage() + " from " + m.getSource() + ")");
                            return;
                    }
                    receivedList.add(textMessage);
                    int expected = messageSeqMap.get(textMessage.getSource());
                    if (textMessage.getSequenceNumber() > expected) {
                        //TODO: optimize
                        for (int i = expected; i < textMessage.getSequenceNumber(); ++i) {
                            sendMessageHelper(new NackMessage(targetAddress, textMessage.getSource(), i));
                        }
                    } else {
                        deliverAll();
                    }
                } else {
                    System.out.println("\t[DROP] not in view! " + view);
                }
                break;
            case 'A': // ack
                AckMessage ackMessage = (AckMessage) m;
                if (ackMessage.getTarget().equals(myAddress)) {
                    TextMessage t = sentUnstableMessages.get(ackMessage.getSequenceNumber());
                    if (t != null) {
                        List<InetAddress> l = t.getAckList();
                        l.remove(ackMessage.getSource());
                        if (l.isEmpty()) { // all other processes in the view acknowledge
                            sentUnstableMessages.remove(ackMessage.getSequenceNumber());
                            System.out.println("\t[ACK] message number " + t.getSequenceNumber() + " completely acknowledged (" + sentUnstableMessages.size() + " left)");
                        }
                    }
                } else {
                    // case of stabilizing received messages
                    Map<Integer, Pair<List<InetAddress>, TextMessage>> map = receivedUnstableMessages.get(ackMessage.getTarget());
                    if (map == null) break;
                    Pair<List<InetAddress>, TextMessage> pair = map.get(ackMessage.getSequenceNumber());
                    if (pair == null) break;
                    pair.fst.remove(ackMessage.getSource());
                    if (pair.fst.isEmpty()) {
                        System.out.println("\t[ACK] message number " + pair.snd.getSequenceNumber() + " from " + pair.snd.getSource() +  " completely acknowledged and ready to deliver!");
                        map.remove(ackMessage.getSequenceNumber());
                        if (!map.keySet().isEmpty()) {
                            List<Integer> missing = new ArrayList<>(map.keySet());
                            Collections.sort(missing);
                            for (int i : missing) {
                                if (i < pair.snd.getSequenceNumber()) {
                                    Pair<List<InetAddress>, TextMessage> otherPair = map.get(i);
                                    System.out.println("\t\t[ACK] assuming delivery of " + i);
                                    map.remove(i);
                                    deliveredQueue.put(otherPair.snd);
                                }
                            }
                        }
                        deliveredQueue.put(pair.snd);
                    }
                }
                break;
            case 'N':
                NackMessage nackMessage = (NackMessage) m;
                System.out.println("[NACK] " + nackMessage.getRequestedMessage());
                if (myAddress.equals(nackMessage.getTargetId())) {
                    if (sentUnstableMessages.containsKey(nackMessage.getRequestedMessage()))
                        sendMessageHelper(sentUnstableMessages.get(nackMessage.getRequestedMessage()));
                    else
                        System.out.println("[NACK] ERROR " + nackMessage.getRequestedMessage());
                }
                break;
            default:
                state = state.processMessage(m);
                break;
        }
    }

    /**
     * This function sends all the pending text messages and adds them to the {@Link sentUnstableMessages}
     */
    public void sendAllPending() {
        System.out.println("[FLUSH] sending all pending text messages");
        while (!toSend.isEmpty()) {
            TextMessage m = toSend.remove();
            sendMessageHelper(m);
            sentUnstableMessages.put(m.getSequenceNumber(), m);
        }
    }

    public Map<InetAddress, Integer> getMessageSeqMap() {
        return messageSeqMap;
    }

    public void doFlush() {
        Set<Integer> unstableIdsSet = sentUnstableMessages.keySet();
        Integer[] unstableIds = unstableIdsSet.toArray(new Integer[unstableIdsSet.size()]);
        Arrays.sort(unstableIds);
        for (Integer i : unstableIds) {
            sendMessageHelper(sentUnstableMessages.get(i));
        }
        sentUnstableMessages.clear();

        sendMessageHelper(new FlushMessage(targetAddress, sequenceNumber));
    }

    /**
     * This function delivers all messages that have a value of {@Link sequenceNumber} equal to the one {@Link expected}
     * discards the ones with lower value and keeps the one with higher value
     * @throws InterruptedException InterruptedException
     */
    public void deliverAll() {
        System.out.println("\t[DELIVER TEXT] preparing messages in FIFO order for delivery");
        boolean redo = true;
        while (redo) {
            redo = false;
            List<TextMessage> toRemove = new LinkedList<>();
            for (TextMessage m : receivedList) {
                int expected = messageSeqMap.get(m.getSource());
                if (m.getSequenceNumber() == expected) {
                    messageSeqMap.put(m.getSource(), expected + 1);
                    if (view.size() > 2) {
                        Map<Integer, Pair<List<InetAddress>, TextMessage>> map = receivedUnstableMessages.get(m.getSource());
                        if (map == null) map = new HashMap<>();
                        Pair<List<InetAddress>, TextMessage> pair = new Pair<>(new ArrayList<>(view), m);
                        // acknowledgement required by all other processes in the view who are not the sender
                        pair.fst.remove(myAddress);
                        pair.fst.remove(m.getSource());

                        map.put(m.getSequenceNumber(), pair);
                        receivedUnstableMessages.put(m.getSource(), map);
                    } else {
                        try {
                            deliveredQueue.put(m);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    AckMessage ackMessage = new AckMessage(targetAddress, m.getSource(), m.getSequenceNumber());
                    sendMessageHelper(ackMessage);
                    for (InetAddress addr : receivedUnstableMessages.keySet()) {
                        Map<Integer, Pair<List<InetAddress>, TextMessage>> map = receivedUnstableMessages.get(addr);
                        for (int num : map.keySet()) {
                            sendMessageHelper(new AckMessage(targetAddress, addr, num));
                        }
                    }
                    redo = true;
                } else if (m.getSequenceNumber() < expected) {
                    toRemove.add(m);
                }
            }
            receivedList.removeAll(toRemove);
        }
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public InetAddress getAddress() {
        return myAddress;
    }

    public void installView(List<InetAddress> view) {
        this.view = new ArrayList<>(view);
    }

    public void sendTextMessage(String message) {
        TextMessage m = new TextMessage(myAddress, message, sequenceNumber++);
        state.sendTextMessage(m);
    }

    public void setView(List<InetAddress> view) {
        this.view = new ArrayList<>(view);
    }
}
