package lib.supervisor.state;

import lib.message.Message;
import lib.supervisor.Supervisor;

import java.io.IOException;

public abstract class SupervisorState {
    protected final Supervisor supervisor;

    public SupervisorState(Supervisor supervisor) {
        this.supervisor = supervisor;
    }
    public abstract SupervisorState processMessage(Message m) throws IOException;
}