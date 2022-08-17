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
import java.util.Collections;
import java.util.List;

public class NormalState extends ClientState {
    private Thread pingingThread;
    public volatile boolean running;
    private final List<InetAddress> view;

    public NormalState(ReliableBroadcastLibrary library, List<InetAddress> view) {
        super(library);
        this.view = new ArrayList<>(view);
        library.setView(view);
        System.out.println("[NORMAL] new view is " + view);
        running = true;

        pingingThread = new PingingThread(this);
        pingingThread.start();
    }

    @Override
    public ClientState processMessage(Message m) throws IOException {
        switch (m.getType()) {
            case 'V':
                close();
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
        if (Settings.UNORDERED) {
            Settings.unorderedMessagesList.add(m);
            if (Math.random() < Settings.UNORDERED_CHANCE) { // if chance then send accumulated message in reverse, else only accumulate
                if (Settings.unorderedMessagesList.size() > 1) System.out.println("[UNORDERED] SENDING " + Settings.unorderedMessagesList.size() + " MESSAGES IN REVERSE!");
                while (!Settings.unorderedMessagesList.isEmpty()) {
                    library.sendMessageHelper(Settings.unorderedMessagesList.remove(Settings.unorderedMessagesList.size() - 1));
                }
            }
        }
        else {
            library.sendMessageHelper(m);
        }
    }

    @Override
    public void close() {
        running = false;
        try {
            pingingThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class PingingThread extends Thread {
    private final NormalState state;

    public PingingThread(NormalState state) {
        this.state = state;
    }

    @Override
    public void run() {
        try {
            while (state.running) {
                state.library.sendMessageHelper(new PingMessage(state.library.getAddress()));
                Thread.sleep(Settings.PING_PERIOD);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
