package lib.supervisor.state;

import lib.message.Message;
import lib.supervisor.Supervisor;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

public abstract class SupervisorState {
    protected final Supervisor supervisor;

    public SupervisorState(Supervisor supervisor) {
        this.supervisor = supervisor;
    }
    public abstract SupervisorState processMessage(Message m) throws IOException;

    public SupervisorState processDisconnect(List<InetAddress> disconnectList) throws IOException {
        return this;
    };
}