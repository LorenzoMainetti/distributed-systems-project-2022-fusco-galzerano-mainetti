package lib.message;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class TextMessage implements Message {
    private final String message;
    private final InetAddress source;
    private final int sequenceNumber;
    private List<InetAddress> ackList;

    public TextMessage(InetAddress source, String message, int sequenceNumber) {
        this.message = message;
        this.source = source;
        this.sequenceNumber = sequenceNumber;
        ackList = new ArrayList<>();
    }

    @Override
    public char getType() { return 'T'; }

    @Override
    public String getTransmissionString() {
        String sequenceString = Integer.toString(sequenceNumber);
        return "T|" + sequenceString + "|" + message + "|";
    }

    public String getMessage() {
        return message;
    }

    @Override
    public InetAddress getSource() {
        return source;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public List<InetAddress> getAckList() {
        return ackList;
    }

    public void setAckList(List<InetAddress> ackList) {
        this.ackList = new ArrayList<>(ackList);
    }
}
