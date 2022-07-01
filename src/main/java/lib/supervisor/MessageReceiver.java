package lib.supervisor;

import lib.message.Message;

import java.io.IOException;

public class MessageReceiver extends Thread {

    private final Supervisor supervisor;

    public MessageReceiver(Supervisor supervisor) {
        this.supervisor = supervisor;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Message m = supervisor.receiveMessage();
                supervisor.putMessage(m);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
