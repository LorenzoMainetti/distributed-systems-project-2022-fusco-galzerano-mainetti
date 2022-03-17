package lib.message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Queue;

public class ReceiverThread extends Thread {
    private DatagramSocket inSocket;
    private Queue<Message> messageQueue;

    public ReceiverThread(DatagramSocket inSocket, Queue<Message> messageQueue) {
        this.inSocket = inSocket;
        this.messageQueue = messageQueue;
    }

    public void run() {
        try {
            while (true) {
                byte[] in = new byte[256];
                DatagramPacket packet = new DatagramPacket(in, in.length);
                inSocket.receive(packet);
                Message m = Message.parseString(new String(packet.getData()), packet.getAddress());

                messageQueue.add(m);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
