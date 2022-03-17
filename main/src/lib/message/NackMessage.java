package lib.message;

import java.net.InetAddress;

public class NackMessage implements Message {
    private InetAddress senderId;
    private int requestedMessage;

    public NackMessage(InetAddress senderId, int requestedMessage) {
        this.senderId = senderId;
        this.requestedMessage = requestedMessage;
    }

    public char getType() { return 'N'; }

    public InetAddress getSenderId() {
        return senderId;
    }

    public int getRequestedMessage() {
        return requestedMessage;
    }
}
