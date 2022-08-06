package lib.supervisor.state;

import lib.message.JoinMessage;
import lib.message.Message;
import lib.message.ViewChangeMessage;
import lib.supervisor.Supervisor;

import java.io.IOException;

public class NormalState extends SupervisorState {
    public NormalState(Supervisor supervisor) {
        super(supervisor);
        System.out.println("[SUPERVISOR] normal state with view: " + supervisor.getView());
    }

    @Override
    public SupervisorState processMessage(Message m) throws IOException {
        if (m.getType() == 'J') {
            JoinMessage joinMessage = (JoinMessage) m;
            supervisor.getView().add(joinMessage.getSource());

            return new ProposeViewChangeState(supervisor);
        }

        return this;
    }

}