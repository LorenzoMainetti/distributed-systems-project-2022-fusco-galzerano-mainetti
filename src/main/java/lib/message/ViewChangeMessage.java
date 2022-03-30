package lib.message;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class ViewChangeMessage implements Message {
    private List<InetAddress> view;
    private InetAddress source;

    public ViewChangeMessage(InetAddress source, List<InetAddress> view) {
        this.source = source;
        this.view = new ArrayList<>(view);
    }

    public char getType() {
        return 'V';
    }

    public InetAddress getSource() { return source; }

    public String getTransmissionString() {
        StringBuilder s = new StringBuilder();
        s.append("V|");
        s.append(view.size());
        s.append("|");
        for (InetAddress address : view) {
            s.append(address);
            s.append("|");
        }

        return s.toString();
    }

    public List<InetAddress> getView() {
        return view;
    }
}
