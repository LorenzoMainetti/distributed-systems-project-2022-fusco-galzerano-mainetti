package lib.message;

import java.net.InetAddress;

public class BeginMessage implements Message {
    private final InetAddress source;

    public BeginMessage(InetAddress source) {
        this.source = source;
    }

    @Override
    public char getType() {
        return 'B';
    }

    @Override
    public InetAddress getSource() {
        return source;
    }

    @Override
    public String getTransmissionString() {
        return "B|";
    }
}
