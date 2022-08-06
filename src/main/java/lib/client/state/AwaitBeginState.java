package lib.client.state;

import lib.client.ReliableBroadcastLibrary;
import lib.message.Message;
import lib.message.ViewChangeAcceptMessage;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

public class AwaitBeginState extends ClientState {
    private final List<InetAddress> view;

    public AwaitBeginState(ReliableBroadcastLibrary library, List<InetAddress> view) {
        super(library);
        this.view = view;
        library.sendMessageHelper(new ViewChangeAcceptMessage(library.getAddress()));
        System.out.println("[VIEWCHANGE] awaiting for viewchange begin confirmation");
    }

    @Override
    public ClientState processMessage(Message m) throws IOException {
        if (m.getType() == 'B') {
            System.out.println("[VIEWCHANGE] beginning");
            library.doFlush();
            if (view.size() <= 1) {
                return new NormalState(library, view);
            } else {
                return new ViewChangeState(library, view);
            }
        }
        return this;
    }
}
