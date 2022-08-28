package lib.client.state;

import lib.Settings;
import lib.Test;
import lib.client.ReliableBroadcastLibrary;
import lib.message.Message;
import lib.message.PingMessage;
import lib.message.TextMessage;
import lib.message.ViewChangeMessage;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
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
                library.getReceivedUnstableMessages().clear();
                if (!viewChangeMessage.getView().contains(library.getAddress())) {
                    System.out.println("[VIEWCHANGE] not in viewchange! Reverting to joining state");
                    return new JoiningState(library);
                } else {
                    library.doFlush();
                    List<InetAddress> newView = viewChangeMessage.getView();
                    if (newView.size() <= 1) {
                        return new NormalState(library, newView);
                    } else {
                        return new ViewChangeState(library, newView);
                    }
                }
        }
        return this;
    }

    @Override
    public void sendTextMessage(TextMessage m) {
        List<InetAddress> ackList = new ArrayList<>(view);
        try {
            ackList.remove(InetAddress.getLocalHost());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        m.setAckList(ackList);
        if (!ackList.isEmpty()) {
            library.addUnstableMessage(m);
            if (Test.UNORDERED) {
                Test.unorderedMessagesList.add(m);
                if (Math.random() < Test.UNORDERED_CHANCE) { // if chance then send accumulated message in reverse, else only accumulate
                    if (Test.unorderedMessagesList.size() > 1) System.out.println("[UNORDERED] SENDING " + Test.unorderedMessagesList.size() + " MESSAGES IN REVERSE!");
                    while (!Test.unorderedMessagesList.isEmpty()) {
                        library.sendMessageHelper(Test.unorderedMessagesList.remove(Test.unorderedMessagesList.size() - 1));
                    }
                }
            }
            else {
                library.sendMessageHelper(m);
            }
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
