package lib.supervisor;

import lib.message.Message;
import lib.message.PingMessage;
import lib.supervisor.state.NormalState;
import lib.supervisor.state.SupervisorState;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Supervisor extends Thread {
    private SupervisorState state;
    private final InetAddress targetAddress;
    private final InetAddress myAddress;
    private final int port;
    private final MulticastSocket ioSocket;
    private final List<InetAddress> view;

    private final BlockingQueue<Message> messageQueue;


    public static void main(String[] args) throws Exception {
        Supervisor supervisor = new Supervisor("224.0.0.1", 8888);
        supervisor.runStateMachine();
    }

    public Supervisor(String address, int port) throws IOException, UnknownHostException {
        targetAddress = InetAddress.getByName(address);
        this.port = port;
        myAddress = InetAddress.getLocalHost();
        ioSocket = new MulticastSocket(port);
        ioSocket.joinGroup(targetAddress);

        view = new ArrayList<>();
        messageQueue = new LinkedBlockingQueue<>();

        state = new NormalState(this);
    }

    private Message receiveMessage() throws IOException {
        DatagramPacket packet;
        do {
            byte[] in = new byte[2048];
            packet = new DatagramPacket(in, in.length);
            ioSocket.receive(packet);
        } while (myAddress.equals(packet.getAddress()));

        return Message.parseString(new String(packet.getData()), packet.getAddress());
    }

    public void runStateMachine() throws IOException, InterruptedException {
        this.run();
        while (true) {
            Message m = messageQueue.take();
            if (m.getType() == 'P') {
                handlePing((PingMessage) m);
            } else {
                state = state.processMessage(m);
            }
        }
    }

    private void handlePing(PingMessage m) {

    }

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

    public void run() {
        while (true) {
            try {
                Message m = receiveMessage();
                messageQueue.put(m);
            } catch (InterruptedException | IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}