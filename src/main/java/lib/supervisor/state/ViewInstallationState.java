package lib.supervisor.state;

import lib.message.BeginMessage;
import lib.message.Message;
import lib.supervisor.Supervisor;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class ViewInstallationState extends SupervisorState {
    private final List<InetAddress> pendingConfirmations;
    public ViewInstallationState(Supervisor supervisor) throws IOException {
        super(supervisor);
        pendingConfirmations = new ArrayList<>(supervisor.getView());
        supervisor.sendMessage(new BeginMessage(supervisor.getMyAddress()));
        System.out.println("[SUPERVISOR] installing view: " + supervisor.getView());
    }

    @Override
    public SupervisorState processMessage(Message m) throws IOException {
        if (m.getType() == 'F') {
            pendingConfirmations.remove(m.getSource());

            if (pendingConfirmations.isEmpty()) {
                return new NormalState(supervisor);
            }
        }

        return this;
    }
}
