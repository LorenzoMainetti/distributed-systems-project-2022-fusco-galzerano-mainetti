package lib.client.state;

import lib.client.ReliableBroadcastLibrary;
import lib.message.JoinMessage;
import lib.message.Message;
import lib.message.ViewChangeMessage;

import java.io.IOException;

public class JoiningState extends ClientState {
    private boolean running;
    private Thread joiningThread;

    public JoiningState(ReliableBroadcastLibrary library) {
        super(library);
        running = true;
        joiningThread = new Thread(() -> {
            while (running) {
                try {
                    library.sendMessageHelper(new JoinMessage(library.getAddress(), library.getSequenceNumber()));
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        joiningThread.start();
    }

    @Override
    public ClientState processMessage(Message m) throws IOException {
        if (m.getType() == 'V') {
            ViewChangeMessage viewChangeMessage = (ViewChangeMessage) m;
            if (!viewChangeMessage.getView().contains(library.getAddress())) {
                return this;
            } else {
                running = false;
                try {
                    joiningThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return new AwaitBeginState(library, viewChangeMessage.getView());
            }
        }
        return this;
    }

    @Override
    public void close() {
        running = false;
    }
}
