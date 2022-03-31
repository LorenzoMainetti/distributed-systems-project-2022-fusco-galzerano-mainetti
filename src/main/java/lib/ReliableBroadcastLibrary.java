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
    private final Queue<TextMessage> toSend;
    private final Map<Integer, TextMessage> sentUnstableMessages;

    private BroadcastState state;
    private List<InetAddress> partialViewFlushAwaitList;

    private ProcessTimer processTimer;
    private Map<InetAddress, Integer> viewTimers;

    /**
     * constructor
     * @param inetAddr is the address of the process
     * @param inPort is the input port for the process
     * @throws IOException
     */
    public ReliableBroadcastLibrary(String inetAddr, int inPort) throws IOException {
        port = inPort;
        address = InetAddress.getByName(inetAddr);
        outSocket = new MulticastSocket(port);
        inSocket = new DatagramSocket();

        sequenceNumber = 0;
        messageSeqMap = new HashMap<>();
        receivedList = new LinkedList<>();
        sentUnstableMessages = new HashMap<>();
        deliveredQueue = new LinkedBlockingQueue<>();
        toSend = new LinkedList<>();
        view = new ArrayList<>();

        state = BroadcastState.JOINING;
        System.out.println("{"+address.toString()+"} joining");
        //broadcast join message
        sendMessageHelper(new JoinMessage(address, sequenceNumber));
        this.start();
    }

    public List<InetAddress> getView() {
        return view;
    }

    public Map<InetAddress, Integer> getViewTimers() {
        return viewTimers;
    }

    public BroadcastState getLibraryState() { return state; }

    /**
     *This function waits for input packets to arrive; it then calls other functions for receipt and delivery
     */
    @Override
    public void run() {

        try {

            processTimer = new ProcessTimer(this);
            new Thread(processTimer).start();

            // Send a ping each 5 seconds.
            new Thread(() -> {
                while (state != BroadcastState.DISCONNECTED) { //isConnected=true
                    try {
                        System.out.println("{"+address.toString()+"} sending ping");
                        sendMessageHelper(new PingMessage(this.address));
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        // endConnection();
                        e.printStackTrace();
                    }
                }
            }).start();

            while (state != BroadcastState.DISCONNECTED) {
                byte[] in = new byte[2048];
                DatagramPacket packet = new DatagramPacket(in, in.length);
                inSocket.receive(packet);
                Message m = Message.parseString(new String(packet.getData()), packet.getAddress());

                receiveMessage(m);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * This function creates a datagramPacket to be sent through the {@Link outSocket}
     * @param m message to be sent
     */
    private void sendMessageHelper(Message m) {
        byte[] buf = m.getTransmissionString().getBytes();
        try {
            System.out.println("{"+address.toString()+"} sending message");
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
            outSocket.send(packet);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This function creates a text message that is then sent through another function
     * @param text is the string to insert in the message
     */
    public void sendTextMessage(String text) {
        TextMessage textMessage = new TextMessage(address, text, sequenceNumber);
        if (state == BroadcastState.NORMAL) {
            System.out.println("{"+address.toString()+"} sending text message when state is normal");
            sendMessageHelper(textMessage);
            sentUnstableMessages.put(sequenceNumber, textMessage);
        } else { // add to queue, all messages in queue will be sent when state goes back to normal
            System.out.println("{"+address.toString()+"} state is not normal, message is added to queue");
            toSend.add(textMessage);
        }
        sequenceNumber++;
    }

    /**
     * This function  is used to send to all the processes in the group a message to inform that the view has changed
     * @param viewChanged is the new view that is the new list of processes
     */
    public void sendViewChangeMessage(List<InetAddress> viewChanged) {
        ViewChangeMessage viewChangeMessage = new ViewChangeMessage(address, viewChanged);
        if (state == BroadcastState.NORMAL) {
            System.out.println("{"+address.toString()+"} sending view change");
            sendMessageHelper(viewChangeMessage);
        } else {
            System.out.println("{"+address.toString()+"} view change is already ongoing");
            // TODO decide how to handle view change when there is another one ongoing
        }
        sequenceNumber++;
    }

    /**
     * @return the last message in the queue of delivered messages
     * @throws InterruptedException
     */
    public TextMessage getTextMessage() throws InterruptedException {
        if(state != BroadcastState.DISCONNECTED) {
            System.out.println("{"+address.toString()+"} delivering messages");
            return deliveredQueue.take();
        }
        else {
            System.out.println("{"+address.toString()+"} process is disconnected");
            throw new InterruptedException();
        }
    }

    /**
     * This function is used to inform the other processes in the group that the process is leaving through a message
     * and then exit
     */
    public void leaveGroup() {
        System.out.println("{"+address.toString()+"} disconnecting");
        sendMessageHelper(new LeaveMessage(address, sequenceNumber));
        state = BroadcastState.DISCONNECTED;
        //TODO System.exit(0) lo fa l'applicazione
    }

    /**
     * This function is used to receive messages and do different operations depending on the type of message received
     * @param m is the received message that has to be processed
     */
    private void receiveMessage(Message m) throws InterruptedException {
        List<InetAddress> newView;
        switch (m.getType()) {
            case 'T':
                System.out.println("{"+address.toString()+"} receiving text message");
                TextMessage textMessage = (TextMessage) m;
                receivedList.add(textMessage);
                int expected = messageSeqMap.get(textMessage.getSource());
                if (textMessage.getSequenceNumber() > expected) {
                    //TODO: optimize
                    for (int i = expected; i < textMessage.getSequenceNumber(); ++i) {
                        sendMessageHelper(new NackMessage(address, textMessage.getSource(),  i));
                    }
                } else {
                    deliverAll();
                }
            case 'A': // ack
                System.out.println("{"+address.toString()+"} receiving ack message");
                assert m instanceof AckMessage;
                AckMessage ackMessage = (AckMessage) m;
                if (ackMessage.getTarget().equals(address)) {
                    TextMessage t = sentUnstableMessages.get(ackMessage.getSequenceNumber());
                    t.incrementAckCount();
                    if (t.getAckCount() == view.size()) { // all other processes in the view acknowledge
                        sentUnstableMessages.remove(t);
                    }
                }
            case 'N':
                System.out.println("{"+address.toString()+"} receiving nack message");
                assert m instanceof NackMessage;
                NackMessage nackMessage = (NackMessage) m;
                if (nackMessage.getTargetId() == address)
                    sendMessageHelper(sentUnstableMessages.get(nackMessage.getRequestedMessage()));
            case 'J':
                System.out.println("{"+address.toString()+"} receiving join message");
                assert m instanceof JoinMessage;
                JoinMessage joinMessage = (JoinMessage) m;
                //messageSeqMap.put(joinMessage.getAddress(), joinMessage.getSequenceNumber());
                newView = new ArrayList<>(view);
                newView.add(joinMessage.getSource());
                beginViewChange(newView);
            case 'L':
                System.out.println("{"+address.toString()+"} receiving leave message");
                assert m instanceof LeaveMessage;
                LeaveMessage leaveMessage = (LeaveMessage) m;
                newView = new ArrayList<>(view);
                newView.remove(leaveMessage.getSource());
                beginViewChange(view);
            case 'V':
                System.out.println("{"+address.toString()+"} receiving view change message");
                assert m instanceof ViewChangeMessage;
                ViewChangeMessage viewChangeMessage = (ViewChangeMessage) m;
                if (state != BroadcastState.VIEWCHANGE) {
                    beginViewChange(viewChangeMessage.getView());
                }
            case 'F':
                System.out.println("{"+address.toString()+"} receiving flush message");
                assert m instanceof FlushMessage;
                FlushMessage flushMessage = (FlushMessage) m;
                if (state == BroadcastState.VIEWCHANGE) { // should always be true
                    processFlushMessage(flushMessage);
                }
            case 'P':
                System.out.println("{"+address.toString()+"} receiving ping message");
                assert m instanceof PingMessage;
                PingMessage pingMessage = (PingMessage) m;
                if (state != BroadcastState.VIEWCHANGE) { // should always be true
                    //reset timer for the specific process in the view
                    processTimer.resetTime(m.getSource());

                }
            default:

        }
    }

    /**
     * This function sends all the pending text messages and adds them to the {@Link sentUnstableMessages}
     */
    private void sendAllPending() {
        System.out.println("{"+address.toString()+"} sending all pending text messages");
        while (!toSend.isEmpty()) {
            TextMessage m = toSend.remove();
            sendMessageHelper(m);
            sentUnstableMessages.put(m.getSequenceNumber(), m);
        }
    }

    /**
     * This function is used to flush messages of the given process by sending all pending messages and deliver them
     * @param flushMessage is the message received
     * @throws InterruptedException
     */
    private void processFlushMessage(FlushMessage flushMessage) throws InterruptedException {
        InetAddress source = flushMessage.getSource();
        partialViewFlushAwaitList.remove(source);
        if (partialViewFlushAwaitList.isEmpty()) {
            System.out.println("{"+address.toString()+"} back to normal broadcast state");
            state = BroadcastState.NORMAL;
            sendAllPending();
            deliverAll();
        }
    }

    private void beginViewChange(List<InetAddress> newView) {
        System.out.println("{"+address.toString()+"} beginning view change");
        state = BroadcastState.VIEWCHANGE;

        Set<Integer> unstableIdsSet = sentUnstableMessages.keySet();
        Integer[] unstableIds = unstableIdsSet.toArray(new Integer[unstableIdsSet.size()]);
        Arrays.sort(unstableIds);
        for (Integer i : unstableIds) {
            sendMessageHelper(sentUnstableMessages.get(i));
        }
        sentUnstableMessages.clear();

        sendMessageHelper(new FlushMessage(address));
        // wait to receive all flush from all other components of the view
        partialViewFlushAwaitList = new ArrayList<>(newView);
        // TODO: wait
    }

    /**
     * This function delivers all messages that have a value of {@Link sequenceNumber} equal to the one {@Link expected}
     * discards the ones with lower value and keeps the one with higher value
     * @throws InterruptedException
     */
    private void deliverAll() throws InterruptedException {
        System.out.println("{"+address.toString()+"} delivering messages in FIFO order");
        boolean redo = true;
        while (redo) {
            redo = false;
            List<TextMessage> toRemove = new LinkedList<>();
            for (TextMessage m : receivedList) {
                int expected = messageSeqMap.get(m.getSource());
                if (m.getSequenceNumber() == expected) {
                    messageSeqMap.put(m.getSource(), expected + 1);
                    deliveredQueue.put(m);
                    AckMessage ackMessage = new AckMessage(address, m.getSource(), m.getSequenceNumber());
                    sendMessageHelper(ackMessage);
                    redo = true;
                } else if (m.getSequenceNumber() < expected) {
                    toRemove.add(m);
                }
            }

            receivedList.removeAll(toRemove);
        }
    }
}
