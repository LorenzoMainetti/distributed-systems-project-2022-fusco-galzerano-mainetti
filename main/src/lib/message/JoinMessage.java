package lib.message;

import java.net.InetAddress;

public class JoinMessage implements Message {
    private final InetAddress address;
    private final int sequenceNumber;

    public JoinMessage(InetAddress address, int sequenceNumber) {
        this.address = address;
        this.sequenceNumber = sequenceNumber;
    }

    @Override
    public char getType() {
        return 'J';
    }

    @Override
    public String getTransmissionString() {
        return "J|" + address.toString() + "|" + sequenceNumber;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }
}
