package lib_imported;

import java.io.IOException;
import java.net.*;


/**
 This class listens for broadcast messages and reports them to the
 ReliableBroadcastProcess that spawned it.
 */
public class RBListenerThread extends Thread
{
    //RB process that this class corresponds to.
    private ReliableBroadcastProcess rbp;

    //Network connection details.
    private int socketNum;
    private NetworkInterface netIf;
    private InetSocketAddress group;

    /**
     Constructor.

     @param rbp  Process that this thread belongs to.
     @param socketNum  Socket on which to listen.
     */
    public RBListenerThread(ReliableBroadcastProcess rbp, NetworkInterface netIf,
                            InetSocketAddress group, int socketNum)
    {
        this.rbp = rbp;
        this.netIf = netIf;
        this.socketNum = socketNum;
        this.group = group;
    }

    /**
     Starts the thread.  It will listen for broadcast messages and report
     them to its corresponding process.

     @see java.lang.Runnable#run()
     */
    public void run()
    {
        MulticastSocket socket;
        try
        {
            socket = new MulticastSocket(socketNum);
            socket.joinGroup(group, netIf);

            DatagramPacket packet;
            while (true)
            {
                byte[] buf = new byte[256];
                packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String received = new String(packet.getData());

                //Here we parse the data received to construct a
                // new Message object from it.
                Message m = Message.parseTransmissionString(received.trim());

                //The message is passed to the RB process here.
                rbp.receive(m);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

}
