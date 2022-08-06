package lib.supervisor.state;

import lib.message.Message;
import lib.message.ViewChangeMessage;
import lib.supervisor.Supervisor;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class ProposeViewChangeState extends SupervisorState {
    private final List<InetAddress> pendingConfirmations;
    public ProposeViewChangeState(Supervisor supervisor) throws IOException {
        super(supervisor);
        pendingConfirmations = new ArrayList<>(supervisor.getView());
        System.out.println("[SUPERVISOR] proposing view: " + supervisor.getView());
    }

    @Override
    public SupervisorState processMessage(Message m) throws IOException {
        if (m.getType() == 'C') {
            pendingConfirmations.remove(m.getSource());
            System.out.println("[CONFIRMATION] received from " + m.getSource() + ", " + pendingConfirmations.size() + " pending");

            if (pendingConfirmations.isEmpty()) {
                return new ViewInstallationState(supervisor);
            }
        }

        return this;
    }
}
