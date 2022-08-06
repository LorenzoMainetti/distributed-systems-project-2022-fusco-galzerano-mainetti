package lib;

import lib.message.Message;

import java.io.IOException;

public interface Receiver {
    Message receiveMessage() throws IOException;

    void putMessage(Message m) throws InterruptedException, IOException;
}
