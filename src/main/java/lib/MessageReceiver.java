package lib;

import lib.message.Message;
import lib.supervisor.Supervisor;

import java.io.IOException;

public class MessageReceiver extends Thread {

    private final Receiver receiver;

    public MessageReceiver(Receiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Message m = receiver.receiveMessage();
                receiver.putMessage(m);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
