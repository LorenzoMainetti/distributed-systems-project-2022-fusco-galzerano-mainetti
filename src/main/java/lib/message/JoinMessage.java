package lib.message;

import java.net.InetAddress;

public class JoinMessage implements Message {
    private final int sequenceNumber;
    private final InetAddress source;

    public JoinMessage(InetAddress source, int sequenceNumber) {
        this.source = source;
        this.sequenceNumber = sequenceNumber;
    }

    @Override
    public char getType() {
        return 'J';
    }

    @Override
    public String getTransmissionString() {
        return "J|" + sequenceNumber + "|";
    }

    @Override
    public InetAddress getSource() { return source; }

    public int getSequenceNumber() {
        return sequenceNumber;
    }
}
