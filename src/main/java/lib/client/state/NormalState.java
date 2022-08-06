package lib.client.state;

import lib.client.ReliableBroadcastLibrary;
import lib.message.Message;
import lib.message.TextMessage;
import lib.message.ViewChangeMessage;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class NormalState extends ClientState {
    private final List<InetAddress> view;

    public NormalState(ReliableBroadcastLibrary library, List<InetAddress> view) {
        super(library);
        this.view = new ArrayList<>(view);
        library.setView(view);
        System.out.println("[NORMAL] new view is " + view);
    }

    @Override
    public ClientState processMessage(Message m) throws IOException {
        switch (m.getType()) {
            case 'V':
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
