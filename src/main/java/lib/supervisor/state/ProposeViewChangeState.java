package lib.supervisor.state;

import lib.message.Message;
import lib.supervisor.Supervisor;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class ProposeViewChangeState extends SupervisorState {
    private final List<InetAddress> pendingConfirmations;
    public ProposeViewChangeState(Supervisor supervisor) {
        super(supervisor);
        pendingConfirmations = new ArrayList<>(supervisor.getView());
    }

    @Override
    public SupervisorState processMessage(Message m) {
        if (m.getType() == 'C') {
            pendingConfirmations.remove(m.getSource());

            if (pendingConfirmations.isEmpty()) {
                return new ViewInstallationState(supervisor);
            }
        }

        return this;
    }
}
