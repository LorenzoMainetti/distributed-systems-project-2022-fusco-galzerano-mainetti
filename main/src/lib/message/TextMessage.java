package lib.message;

import java.net.InetAddress;

public class TextMessage implements Message {
    private String message;
    private InetAddress senderId;
    private int sequenceNumber;

    public TextMessage(String message, InetAddress senderId, int sequenceNumber) {
        this.message = message;
        this.senderId = senderId;
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

    public InetAddress getSenderId() {
        return senderId;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }
}
