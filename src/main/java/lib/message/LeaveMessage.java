package lib.message;

import java.net.InetAddress;

public class LeaveMessage implements Message {
    private final int sequenceNumber;
    private final InetAddress source;

    public LeaveMessage(InetAddress source, int sequenceNumber) {
        this.source = source;
        this.sequenceNumber = sequenceNumber;
    }

    @Override
    public char getType() {
        return 'L';
    }

    @Override
    public String getTransmissionString() {
        return "L|" + source.toString() + "|" + sequenceNumber + "|";
    }

    @Override
    public InetAddress getSource() { return source; }

    public int getSequenceNumber() {
        return sequenceNumber;
    }
}
