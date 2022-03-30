package lib;

import java.net.InetAddress;

/**
 * This class controls if the server is still connected to the network.
 */
public class ProcessTimer implements Runnable{

    public final static int TIME_EXPIRED_MILLIS = 15000; //15 sec
    //private int timeMillis = 0;
    private final ReliableBroadcastLibrary library;
    private boolean isConnected = true;

    public ProcessTimer(ReliableBroadcastLibrary library){
        this.library = library;
    }

    /**
     * This method controls, each milliseconds, that the client is still connected to the server.
     * If not, it calls the {@link ..} and terminates.
     */
    @Override
    public void run() {
        while (true) {
            if( library.getLibraryState() == BroadcastState.NORMAL) {
                for (int i = 0; i < library.getView().size(); i++) {
                    InetAddress source = library.getView().get(i);
                    int timerValue = library.getviewTimers().get(source);

                    if (timerValue > TIME_EXPIRED_MILLIS) {
                        isConnected = false;
                        library.getView().remove(i);
                        library.sendViewChangeMessage(library.getView());
                    }
                    library.getviewTimers().put(source, timerValue + 1000);

                }
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                isConnected = false;
                //e.printStackTrace();
            }

        }

    }

    void setIsConnectedFalse(){
        isConnected = false;
    }

    /**
     * When a ping is listened by the {@link ReliableBroadcastLibrary}, the timer is reset.
     */
    void resetTime(InetAddress source){
        library.getViewTimers().put(source, 0);
    }
}