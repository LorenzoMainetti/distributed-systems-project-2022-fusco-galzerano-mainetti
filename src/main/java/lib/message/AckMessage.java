package lib.message;

import java.net.InetAddress;

public class AckMessage implements Message {
    private final InetAddress source;
    private final InetAddress target;
    private final int sequenceNumber;

    public AckMessage(InetAddress source, InetAddress target, int sequenceNumber) {
        this.source = source;
        this.target = target;
        this.sequenceNumber = sequenceNumber;
    }

    @Override
    public char getType() {
        return 'A';
    }

    @Override
    public InetAddress getSource() {
        return source;
    }

    @Override
    public String getTransmissionString() {
        return "A|" + target.toString().substring(1) + "|" + sequenceNumber + "|";
    }

    public InetAddress getTarget() {
        return target;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }
}
