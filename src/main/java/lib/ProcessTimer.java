package lib;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class controls if the server is still connected to the network.
 */
public class ProcessTimer implements Runnable{

    public final static int TIME_EXPIRED_MILLIS = 15000; //15 sec
    public final static int TIME_SLEEP = 1000;
    //private int timeMillis = 0;
    private final ReliableBroadcastLibrary library;
    private boolean isConnected = true;

    public ProcessTimer(ReliableBroadcastLibrary library){
        this.library = library;
    }

    /**
     * This method controls, each millisecond, that the process is still connected.
     * If not, it calls the {@link ..} and terminates.
     */
    @Override
    public void run() {
        try {
            while (library.getLibraryState() != BroadcastState.DISCONNECTED) {
                if (library.getLibraryState() == BroadcastState.NORMAL) {
                    List<InetAddress> toRemove = new ArrayList<>();
                    for (InetAddress source : library.getView()) {
                        if (InetAddress.getLocalHost().equals(source)) continue;
                        int timerValue = library.getViewTimers().computeIfAbsent(source, k -> 0);

                        if (timerValue > TIME_EXPIRED_MILLIS) {
                            isConnected = false;
                            toRemove.add(source);
                            System.out.println("[WATCHDOG] timer for " + source + " has expired");
                        } else {
                            library.getViewTimers().put(source, timerValue + TIME_SLEEP);
                        }
                    }
                    if (!toRemove.isEmpty()) {
                        library.getView().removeAll(toRemove);
                        library.beginViewChange(library.getView(), true);
                    }
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