package lib.client.state;

import lib.Settings;
import lib.client.ReliableBroadcastLibrary;
import lib.message.Message;
import lib.message.PingMessage;
import lib.message.TextMessage;
import lib.message.ViewChangeMessage;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class NormalState extends ClientState {
    private Thread pingingThread;
    private boolean running;
    private final List<InetAddress> view;

    public NormalState(ReliableBroadcastLibrary library, List<InetAddress> view) {
        super(library);
        this.view = new ArrayList<>(view);
        library.setView(view);
        System.out.println("[NORMAL] new view is " + view);
        running = true;

        pingingThread = new Thread() {
            @Override
            public void run() {
                try {
                    while (running) {
                        library.sendMessageHelper(new PingMessage(library.getAddress()));
                        Thread.sleep(Settings.PING_PERIOD);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        pingingThread.start();
    }

    @Override
    public ClientState processMessage(Message m) throws IOException {
        switch (m.getType()) {
            case 'V':
                running = false;
                ViewChangeMessage viewChangeMessage = (ViewChangeMessage) m;
                if (!viewChangeMessage.getView().contains(library.getAddress())) {
                    System.out.println("[VIEWCHANGE] not in viewchange! Reverting to joining state");
                    return new JoiningState(library);
                } else {
                    return new AwaitBeginState(library, viewChangeMessage.getView());
                }
        }
        return this;
    }

    @Override
    public void sendTextMessage(TextMessage m) {
        library.addUnstableMessage(m);
        library.sendMessageHelper(m);
    }
}
