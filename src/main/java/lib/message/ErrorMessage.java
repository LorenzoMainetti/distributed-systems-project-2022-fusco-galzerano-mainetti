package lib.message;

import java.net.InetAddress;

public class ErrorMessage implements Message {
    private final InetAddress source;

    public ErrorMessage(InetAddress source) {
        this.source = source;
    }

    @Override
    public char getType() { return 'E'; }

    @Override
    public String getTransmissionString() {
        return "E|";
    }

    @Override
    public InetAddress getSource() { return source; }

}
