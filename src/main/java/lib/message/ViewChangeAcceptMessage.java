package lib.message;

import java.net.InetAddress;

public class ViewChangeAcceptMessage implements Message {
    private final InetAddress source;

    public ViewChangeAcceptMessage(InetAddress source) {
        this.source = source;
    }

    @Override
    public char getType() {
        return 'C';
    }

    @Override
    public InetAddress getSource() {
        return null;
    }

    @Override
    public String getTransmissionString() {
        return "C|";
    }
}
