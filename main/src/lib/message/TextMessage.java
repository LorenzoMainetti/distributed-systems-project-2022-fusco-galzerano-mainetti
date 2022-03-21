package lib.message;

import java.net.InetAddress;

public class TextMessage implements Message {
    private final String message;
    private final InetAddress source;
    private final int sequenceNumber;

    public TextMessage(InetAddress source, String message, int sequenceNumber) {
        this.message = message;
        this.source = source;
        this.sequenceNumber = sequenceNumber;
    }

    public char getType() { return 'T'; }

    public String getTransmissionString() {
        String sequenceString = Integer.toString(sequenceNumber);
        return "T|" + sequenceString + "|" + message;
    }

    public String getMessage() {
        return message;
    }

    public InetAddress getSource() {
        return source;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }
}
