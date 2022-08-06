package lib.client.state;

import lib.client.ReliableBroadcastLibrary;
import lib.message.Message;

import java.io.IOException;

public class DisconnectedState extends ClientState {
    public DisconnectedState(ReliableBroadcastLibrary library) throws IOException {
        super(library);
        throw new IOException("DISCONNECTED");
    }

    @Override
    public ClientState processMessage(Message m) throws IOException {
        throw new IOException("DISCONNECTED");
    }
}
