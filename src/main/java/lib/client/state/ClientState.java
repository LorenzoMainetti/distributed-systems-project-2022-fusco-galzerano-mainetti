package lib.client.state;

import lib.client.ReliableBroadcastLibrary;
import lib.message.Message;
import lib.message.TextMessage;
import lib.supervisor.Supervisor;

import java.io.IOException;

public abstract class ClientState {
    protected final ReliableBroadcastLibrary library;

    public ClientState(ReliableBroadcastLibrary library) {
        this.library = library;
    }
    public abstract ClientState processMessage(Message m) throws IOException;

    public void sendTextMessage(TextMessage m) {
        library.addToSend(m);
    }
}