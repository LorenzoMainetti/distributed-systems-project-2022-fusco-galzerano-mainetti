package lib;

import lib.message.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ReliableBroadcastLibrary extends Thread {
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

    private BroadcastState state;
    private List<InetAddress> partialViewFlushAwaitList;

    private ProcessTimer processTimer;
    private Map<InetAddress, Integer> viewTimers;

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
        deliveredQueue = new LinkedBlockingQueue<>();
        toSend = new LinkedList<>();
        view = new ArrayList<>();
        view.add(myAddress);
        viewTimers = new HashMap<>();

        state = BroadcastState.NORMAL;
        System.out.println("[JOIN]");
        //broadcast join message
        sendMessageHelper(new JoinMessage(myAddress, sequenceNumber));
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
     * This function waits for input packets to arrive; it then calls other functions for receipt and delivery
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
                        sendMessageHelper(new PingMessage(this.targetAddress));
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        // endConnection();
                        e.printStackTrace();
                    }
                }
            }).start();

            while (state != BroadcastState.DISCONNECTED) {
                System.out.flush();
                byte[] in = new byte[2048];
                DatagramPacket packet = new DatagramPacket(in, in.length);
                ioSocket.receive(packet);
                if (myAddress.equals(packet.getAddress())) {
                    continue;
                }

                Message m = Message.parseString(new String(packet.getData()), packet.getAddress());

                processMessage(m);
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
            if (m.getType() != 'P')
                System.out.println("[SEND] " + m.getTransmissionString());
            DatagramPacket packet = new DatagramPacket(buf, buf.length, targetAddress, port);
            ioSocket.send(packet);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * API call used internally by ProcessTimer:
     * This function  is used to send to all the processes in the group a message to inform that the view has changed.
     * Sent after a process has failed, i.e. ProcessTimer exceeded
     * @param viewChanged is the new view that is the new list of processes
     */
    protected void sendViewChangeMessage(List<InetAddress> viewChanged) {
        ViewChangeMessage viewChangeMessage = new ViewChangeMessage(targetAddress, viewChanged);
        if (state == BroadcastState.NORMAL) {
            sendMessageHelper(viewChangeMessage);
        } else {
            System.out.println("[SEND VIEWCHANGE] view change is already ongoing");
            // TODO decide how to handle view change when there is another one ongoing
        }
        sequenceNumber++;
    }

    /**
     * API call to send a text message:
     * This function creates a text message that is then sent through another function
     * @param text is the string to insert in the message
     */
    public void sendTextMessage(String text) throws InterruptedException {
        TextMessage textMessage = new TextMessage(targetAddress, text, sequenceNumber);
        if (state == BroadcastState.NORMAL) {
            sentUnstableMessages.put(sequenceNumber, textMessage);
            if (Settings.T_FAULT && Math.random() < 0.2) {
                System.out.println("[FAULT] dropping text message number " + sequenceNumber);
            } else {
                sendMessageHelper(textMessage);
            }
        } else if (state == BroadcastState.VIEWCHANGE) { // add to queue, all messages in queue will be sent when state goes back to normal
            System.out.println("[SEND TEXT] currently in viewchange, message is added to queue");
            toSend.add(textMessage);
        } else if (state == BroadcastState.DISCONNECTED) {
            throw new InterruptedException();
        }
        sequenceNumber++;
    }

    /**
     * API call to retrieve the last delivered message:
     * @return the last message in the queue of delivered messages
     * @throws InterruptedException
     */
    public TextMessage getTextMessage() throws InterruptedException {
        if(state != BroadcastState.DISCONNECTED) {
            System.out.println("[DELIVER TEXT] delivering message");
            return deliveredQueue.take();
        }
        else {
            System.out.println("[DELIVER TEXT] process is disconnected");
            throw new InterruptedException();
        }
    }

    /**
     * API call to leave the group and close the communication:
     * This function is used to inform the other processes in the group that the process is leaving through a message
     * and then exit
     */
    public void leaveGroup() {
        System.out.println("[DISCONNECT]");
        sendMessageHelper(new LeaveMessage(targetAddress, sequenceNumber));
        state = BroadcastState.DISCONNECTED;
        //TODO System.exit(0) is done by the app
    }

    /**
     * This function is used to receive messages and do different operations depending on the type of message received
     * @param m is the received message that has to be processed
     */
    private void processMessage(Message m) throws InterruptedException, UnknownHostException {
        List<InetAddress> newView;
        if (m.getType() != 'P')
            System.out.println("[RECEIVE] processing " + m.getType() + " message from " + m.getSource().getCanonicalHostName());
        switch (m.getType()) {
            case 'T':
                if(view.contains(m.getSource())) {
                    TextMessage textMessage = (TextMessage) m;
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
                    System.out.println("\t[DROP] not in view!");
                }
                break;
            case 'A': // ack
                assert m instanceof AckMessage;
                AckMessage ackMessage = (AckMessage) m;
                if (ackMessage.getTarget().equals(myAddress)) {
                    TextMessage t = sentUnstableMessages.get(ackMessage.getSequenceNumber());
                    t.incrementAckCount();
                    if (t.getAckCount() == view.size() - 1) { // all other processes in the view acknowledge
                        sentUnstableMessages.remove(ackMessage.getSequenceNumber());
                        System.out.println("\t[ACK] message number " + t.getSequenceNumber() + " completely acknowledged (" + sentUnstableMessages.size() + " left)");
                    }
                }
                break;
            case 'N':
                assert m instanceof NackMessage;
                NackMessage nackMessage = (NackMessage) m;
                if (myAddress.equals(nackMessage.getTargetId()))
                    sendMessageHelper(sentUnstableMessages.get(nackMessage.getRequestedMessage()));
                break;
            case 'J':
                assert m instanceof JoinMessage;
                JoinMessage joinMessage = (JoinMessage) m;
                newView = new ArrayList<>(view);
                newView.add(joinMessage.getSource());
                beginViewChange(newView, true);
                break;
            case 'L':
                assert m instanceof LeaveMessage;
                LeaveMessage leaveMessage = (LeaveMessage) m;
                newView = new ArrayList<>(view);
                newView.remove(leaveMessage.getSource());
                beginViewChange(view, true);
                break;
            case 'V':
                assert m instanceof ViewChangeMessage;
                ViewChangeMessage viewChangeMessage = (ViewChangeMessage) m;
                if (!viewChangeMessage.getView().contains(myAddress)) {
                    System.out.println("[VIEWCHANGE] I'm not in the view!");
                    state = BroadcastState.DISCONNECTED;
                    break;
                }
                if (state == BroadcastState.NORMAL) {
                    beginViewChange(viewChangeMessage.getView(), false);
                }
                break;
            case 'F':
                assert m instanceof FlushMessage;
                FlushMessage flushMessage = (FlushMessage) m;
                if (state == BroadcastState.VIEWCHANGE) { // should always be true
                    processFlushMessage(flushMessage);
                }
                break;
            case 'P':
                assert m instanceof PingMessage;
                PingMessage pingMessage = (PingMessage) m;
                if (state != BroadcastState.VIEWCHANGE) { // should always be true
                    //reset timer for the specific process in the view
                    viewTimers.put(pingMessage.getSource(), 0);
                }
                break;
            default:
                break;
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
        messageSeqMap.put(flushMessage.getSource(), flushMessage.getSequenceNumber());
        if (partialViewFlushAwaitList.isEmpty()) {
            System.out.println("[FLUSH] completed, back to normal broadcast state. New view is:");
            System.out.print("\t");
            view = new ArrayList<>(pendingView);
            for (InetAddress address : view) {
                System.out.print(address.toString() + ", ");
            }
            System.out.println("");
            state = BroadcastState.NORMAL;
            sendAllPending();
            deliverAll();
        }
        else {
            System.out.println("[FLUSH] cannot be completed, view change still ongoing");
        }
    }

    /**
     * This function sends all the pending text messages and adds them to the {@Link sentUnstableMessages}
     */
    private void sendAllPending() {
        System.out.println("[FLUSH] sending all pending text messages");
        while (!toSend.isEmpty()) {
            TextMessage m = toSend.remove();
            sendMessageHelper(m);
            sentUnstableMessages.put(m.getSequenceNumber(), m);
        }
    }

    /**
     * API call used internally by ProcessTimer:
     * This function starts a view change resending all unstable messages in order and
     * then send a flush message
     * @param newView
     */
    protected void beginViewChange(List<InetAddress> newView, boolean shouldNotify) throws UnknownHostException {
        System.out.println("\t[VIEWCHANGE] beginning view change");
        state = BroadcastState.VIEWCHANGE;
        pendingView = new ArrayList<>(newView);

        if (shouldNotify)
            sendMessageHelper(new ViewChangeMessage(targetAddress, newView));

        Set<Integer> unstableIdsSet = sentUnstableMessages.keySet();
        Integer[] unstableIds = unstableIdsSet.toArray(new Integer[unstableIdsSet.size()]);
        Arrays.sort(unstableIds);
        for (Integer i : unstableIds) {
            sendMessageHelper(sentUnstableMessages.get(i));
        }
        sentUnstableMessages.clear();

        sendMessageHelper(new FlushMessage(targetAddress, sequenceNumber));
        // wait to receive all flush from all other components of the view
        partialViewFlushAwaitList = new ArrayList<>(newView);
        partialViewFlushAwaitList.remove(myAddress);
    }

    /**
     * This function delivers all messages that have a value of {@Link sequenceNumber} equal to the one {@Link expected}
     * discards the ones with lower value and keeps the one with higher value
     * @throws InterruptedException
     */
    private void deliverAll() throws InterruptedException {
        System.out.println("\t[DELIVER TEXT] preparing messages in FIFO order for delivery");
        boolean redo = true;
        while (redo) {
            redo = false;
            List<TextMessage> toRemove = new LinkedList<>();
            for (TextMessage m : receivedList) {
                int expected = messageSeqMap.get(m.getSource());
                if (m.getSequenceNumber() == expected) {
                    messageSeqMap.put(m.getSource(), expected + 1);
                    deliveredQueue.put(m);
                    AckMessage ackMessage = new AckMessage(targetAddress, m.getSource(), m.getSequenceNumber());
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
