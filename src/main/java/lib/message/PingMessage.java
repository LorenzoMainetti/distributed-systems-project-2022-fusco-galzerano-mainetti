package lib.message;

import java.net.InetAddress;

public class PingMessage implements Message {
    private final InetAddress source;

    public PingMessage(InetAddress source) {
        this.source = source;
    }

    @Override
    public char getType() {
        return 'P';
    }

    @Override
    public String getTransmissionString() {
        return "P|";
    }

    @Override
    public InetAddress getSource() { return source; }
}
