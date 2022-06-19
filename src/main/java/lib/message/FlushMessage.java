package lib.message;

import java.net.InetAddress;

public class FlushMessage implements Message {
    private final InetAddress source;
    private final int sequenceNumber;

    public FlushMessage(InetAddress source, int sequenceNumber) {
        this.source = source;
        this.sequenceNumber = sequenceNumber;
    }

    @Override
    public char getType() {
        return 'F';
    }

    @Override
    public InetAddress getSource() {
        return source;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    @Override
    public String getTransmissionString() {
        return "F|" + sequenceNumber + "|";
    }
}
