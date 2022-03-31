package lib.message;

import java.net.InetAddress;

public class NackMessage implements Message {
    private final InetAddress source;
    private final InetAddress targetId;
    private final int requestedMessage;

    public NackMessage(InetAddress source, InetAddress targetId, int requestedMessage) {
        this.source = source;
        this.targetId = targetId;
        this.requestedMessage = requestedMessage;
    }

    @Override
    public char getType() { return 'N'; }

    @Override
    public InetAddress getSource() {
        return source;
    }

    public int getRequestedMessage() {
        return requestedMessage;
    }

    public InetAddress getTargetId() {
        return targetId;
    }

    @Override
    public String getTransmissionString() {
        return "N|" + targetId.toString() + "|" + requestedMessage;
    }
}
