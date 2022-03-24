package lib.message;

import java.net.InetAddress;

public class AckMessage implements Message {
    private final InetAddress source;

    public AckMessage(InetAddress source) {
        this.source = source;
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
        return "A";
    }
}
