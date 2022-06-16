package lib.message;

import java.net.InetAddress;

public class FlushMessage implements Message {
    private final InetAddress source;

    public FlushMessage(InetAddress source) {
        this.source = source;
    }

    @Override
    public char getType() {
        return 'F';
    }

    @Override
    public InetAddress getSource() {
        return source;
    }

    @Override
    public String getTransmissionString() {
        return "F|";
    }
}
