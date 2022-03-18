package lib.message;

import java.net.InetAddress;

public class PingMessage implements Message {
    private final InetAddress address;

    public PingMessage(InetAddress address) {
        this.address = address;
    }

    @Override
    public char getType() {
        return 'P';
    }

    @Override
    public String getTransmissionString() {
        return "P|" + address.toString();
    }
}
