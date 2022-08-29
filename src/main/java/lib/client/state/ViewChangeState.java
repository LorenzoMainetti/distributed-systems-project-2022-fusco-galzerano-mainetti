package lib.client.state;

import lib.client.ReliableBroadcastLibrary;
import lib.message.FlushMessage;
import lib.message.Message;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class ViewChangeState extends ClientState {
    private final List<InetAddress> pendingView;
    private final List<InetAddress> view;
    private volatile boolean loop;
    private final Thread flushingThread;

    public ViewChangeState(ReliableBroadcastLibrary library, List<InetAddress> view) {
        super(library);
        this.pendingView = new ArrayList<>(view);
        pendingView.remove(library.getAddress());
        this.view = new ArrayList<>(view);

        loop = true;
        flushingThread = new Thread(() -> {
            while (loop) {
                try {
                    library.doFlush();
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        flushingThread.start();
    }

    @Override
    public ClientState processMessage(Message m) throws IOException {
        if (m.getType() == 'F') {
            FlushMessage flushMessage = (FlushMessage) m;
            pendingView.remove(m.getSource());
            library.getMessageSeqMap().put(flushMessage.getSource(), flushMessage.getSequenceNumber());

            if (pendingView.isEmpty()) {
                loop = false;
                try {
                    flushingThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return new NormalState(library, view);
            }
        }
        return this;
    }
}
