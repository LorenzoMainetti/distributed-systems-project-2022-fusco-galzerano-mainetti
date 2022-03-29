package lib;

/**
 * This class controls if the server is still connected to the network.
 */
public class ProcessTimer implements Runnable{

    public final static int TIME_EXPIRED_MILLIS = 15000;
    private int timeMillis = 0;
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
        while (isConnected) {
            try {
                if (timeMillis > TIME_EXPIRED_MILLIS) {
                    isConnected = false;
                }

                Thread.sleep(1);
                timeMillis++;
            } catch (InterruptedException e) {
                isConnected = false;
//                e.printStackTrace();
            }
        }
        // library.endConnection(); TODO implement endConnection?
    }

    void setIsConnectedFalse(){
        isConnected = false;
    }

    /**
     * When a ping is listened by the {@link ReliableBroadcastLibrary}, the timer is reset.
     */
    void resetTime(){
        timeMillis = 0;
    }
}