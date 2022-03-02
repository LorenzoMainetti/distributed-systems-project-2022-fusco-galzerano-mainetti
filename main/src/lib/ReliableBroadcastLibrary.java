package lib;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class ReliableBroadcastLibrary {
    private MulticastSocket outSocket;
    private DatagramSocket inSocket;

    private int port;
    private InetAddress addr;

    public ReliableBroadcastLibrary(String inetAddr, int inPort) throws IOException {
        port = inPort;
        addr = InetAddress.getByName(inetAddr);
        outSocket = new MulticastSocket(port);
        inSocket = new DatagramSocket();
    }
}
