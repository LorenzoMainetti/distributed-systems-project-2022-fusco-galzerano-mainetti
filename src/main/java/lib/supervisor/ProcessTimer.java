package lib.supervisor;

import lib.ReliableBroadcastLibrary;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class controls if the server is still connected to the network.
 */
public class ProcessTimer extends Thread {

    public final static int TIME_EXPIRED_MILLIS = 15000; //15 sec
    public final static int TIME_SLEEP = 1000;
    //private int timeMillis = 0;
    private final Supervisor supervisor;
    private boolean isConnected = true;

    public ProcessTimer(Supervisor supervisor){
        this.supervisor = supervisor;
    }

    /**
     * This method controls, each millisecond, that the process is still connected.
     * If not, it calls the {@link ..} and terminates.
     */
    @Override
    public void run() {
        try {
            while (true) {
                // TODO: synchronize
                List<InetAddress> toRemove = new ArrayList<>();
                for (InetAddress source : supervisor.getView()) {
                    if (InetAddress.getLocalHost().equals(source)) continue;
                    int timerValue = supervisor.getViewTimers().computeIfAbsent(source, k -> 0);

                    if (timerValue > TIME_EXPIRED_MILLIS) {
                        isConnected = false;
                        toRemove.add(source);
                        System.out.println("[WATCHDOG] timer for " + source + " has expired");
                    } else {
                        supervisor.getViewTimers().put(source, timerValue + TIME_SLEEP);
                    }
                }
                if (!toRemove.isEmpty()) {
                    if (supervisor.getView().removeAll(toRemove));
                        //supervisor.beginViewChange(supervisor.getView());
                }

                Thread.sleep(TIME_SLEEP);
            }
        } catch (InterruptedException | UnknownHostException e) {
            isConnected = false;
            e.printStackTrace();
        }
    }

    void setIsConnectedFalse(){
        isConnected = false;
    }
}