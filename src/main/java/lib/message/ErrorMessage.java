package lib.message;

import java.net.InetAddress;

public class ErrorMessage implements Message {
    private final InetAddress source;

    public ErrorMessage(InetAddress source) {
        this.source = source;
    }

    public char getType() { return 'E'; }

    public String getTransmissionString() {
        return "E";
    }

    public InetAddress getSource() { return source; }

}
