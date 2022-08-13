package lib.supervisor;

import lib.MessageReceiver;
import lib.Receiver;
import lib.message.Message;
import lib.message.PingMessage;
import lib.supervisor.state.NormalState;
import lib.supervisor.state.SupervisorState;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Supervisor implements Receiver {
    private SupervisorState state;
    private final InetAddress targetAddress;
    private final InetAddress myAddress;
    private final int port;
    private final MulticastSocket ioSocket;
    private final List<InetAddress> view;

    private final BlockingQueue<Message> messageQueue;
    private final Map<InetAddress, Integer> viewTimers;

    private final MessageReceiver messageReceiver;
    private final ProcessTimer processTimer;

    public static void main(String[] args) throws Exception {
        Supervisor supervisor = new Supervisor("224.0.0.1", 8888);
        supervisor.runStateMachine();
    }

    /**
     * Constructor
     * @param address is the address of the process
     * @param port is the input port for the process
     * @throws IOException
     */
    public Supervisor(String address, int port) throws IOException {
        targetAddress = InetAddress.getByName(address);
        this.port = port;
        myAddress = InetAddress.getLocalHost();
        ioSocket = new MulticastSocket(port);
        ioSocket.joinGroup(targetAddress);

        view = new ArrayList<>();
        messageQueue = new LinkedBlockingQueue<>();
        viewTimers = new HashMap<>();

        state = new NormalState(this);
        messageReceiver = new MessageReceiver(this);
        processTimer = new ProcessTimer(this);
    }

    /**
     * This function creates a datagramPacket to be received through the {@Link ioSocket}
     * @throws IOException
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
     * This function contains the logic of the Supervisor which consists in starting {@Link messageReceiver}
     * and {@Link processTimer} to then handle messages through {@Link messageQueue}
     * @throws IOException
     * @throws InterruptedException
     */
    public void runStateMachine() throws IOException, InterruptedException {
        messageReceiver.start();
        processTimer.start();
        while (true) {
            Message m = messageQueue.take();
            if (isImportantMessage(m.getType()))
                System.out.println("[SUPERVISOR] received: " + m.getTransmissionString() + " from " + m.getSource());
            if (m.getType() == 'P') {
                handlePing((PingMessage) m);
            } else {
                state = state.processMessage(m);
            }
        }
    }

    private boolean isImportantMessage(char type) {
        return false;
    }

    /**
     * This function handles the pings messages by restarting the timer in
     * {@Link viewTimers} for the process who sent the message and is in the view
     * @param m ping message received
     */
    private void handlePing(PingMessage m) {
        viewTimers.put(m.getSource(), 0);
    }

    /**
     * This function creates a datagramPacket to be sent through the {@Link ioSocket}
     * @param m message to be sent
     * @throws IOException
     */
    public void sendMessage(Message m) throws IOException {
        byte[] buf = m.getTransmissionString().getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, targetAddress, port);
        ioSocket.send(packet);
    }

    public List<InetAddress> getView() {
        return view;
    }

    public InetAddress getMyAddress() {
        return myAddress;
    }

    public Map<InetAddress, Integer> getViewTimers() {
        return viewTimers;
    }

    public void putMessage(Message m) throws InterruptedException {
        messageQueue.put(m);
    }

    public void publishDisconnected(List<InetAddress> disconnected) throws IOException {
        if (!disconnected.isEmpty()) {
            state = state.processDisconnect(disconnected);
        }
    }
}