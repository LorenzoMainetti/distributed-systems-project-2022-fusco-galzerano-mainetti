package lib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

/**
 This class represents a Reliable Broadcast Process, using
 the 'message diffusion' algorithm presented by Vassos Hadzilacos
 and Sam Toueg (with some adjustments) presented in Chapter 5 of
 "Distributed Systems", 2nd edition, 1993, Mullender.
 */
public class ReliableBroadcastProcess
{
    //We must track which messages we have received.
    private List<Message> messagesReceived;

    //String that uniquely identifies the process
    protected String procID;

    //Number of messages sent by this process.
    protected int messageSentCount;


    //Connection details for this implementation.
    private static final int port = 4446;
    private NetworkInterface netIf;
    private InetAddress address;
    private InetSocketAddress group;
    private MulticastSocket socket;

    /**
     Constructor.
     @param groupAddress  Address for this broadcast group.
     */
    public ReliableBroadcastProcess(String ID, String groupAddress) throws SocketException {
        procID = ID;
        messageSentCount = 0;
        messagesReceived = new ArrayList<Message>();
        try
        {
            socket = new MulticastSocket(port);
            address = InetAddress.getByName(groupAddress);
            netIf = NetworkInterface.getByName("bge0");
            group = new InetSocketAddress(address, port);
            socket.joinGroup(group, netIf);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     Constructor.  Defaults to use address 230.0.0.1.
     */
    public ReliableBroadcastProcess(String ID) throws SocketException {
        this(ID, "230.0.0.1");
    }

    /**
     Method that represents the send primitive.  It transmits a
     message to all processes, including itself.  Note that the text differed
     slightly from this in that its send primitive was responsible
     only for transmitting a message to a single process, rather
     than transmitting to all processes.  To capture this difference,
     we have renamed this "sendToAll".

     @param m  Message to transmit.
     */
    public void sendToAll(Message m)
    {
        byte[] buf = m.transmissionString().getBytes();
        try
        {
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
            socket.send(packet);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     Method that represents the receive primitive, following the
     text's algorithm.

     @param m  Message received.
     */
    public void receive(Message m)
    {
        //If the message has been received before, it is then ignored.
        if (!messagesReceived.contains(m))
        {
            //We must track messages that we receive so that we do not
            // relay them multiple times.
            messagesReceived.add(m);

            //TODO check if needed
            //Note that the message is relayed to all processes
            // before it is delivered.
            sendToAll(m);
            deliver(m);
        }
    }

    /**
     This broadcasts a new message.  Note that at creation,
     the message is tagged with the process's ID and the
     sequence number of this message.

     @param messageText  Text of message to broadcast.
     */
    public void broadcast(String messageText)
    {
        Message m = new Message(messageText, procID, messageSentCount++);
        sendToAll(m);
    }

    /**
     To simulate delivery, we just print out the message.
     @param m  Message to deliver.
     */
    public void deliver(Message m)
    {
        System.out.println("  Delivered: " + m);
    }



    /**
     Starts the process running.  It will read from standard input and broadcast
     messages for text that the user enters.  Also, this will start the listener
     thread so that it will receive messages broadcast by other processes.
     */
    public void start()
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        startListenerThread();
        while (true)
        {
            try
            {
                String messageText = reader.readLine();
                broadcast(messageText);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     Starts a new thread to listen for broadcast messages.
     */
    protected void startListenerThread()
    {
        RBListenerThread thread = new RBListenerThread(this, netIf, group, port);
        thread.start();
    }

    /**
     Calls the start method for this class.
     */
    public static void main(String [] args) throws SocketException {
        if (args.length < 1)
        {
            System.out.println("Usage: java ReliableBroadcastProcess <process name>");
            System.exit(0);
        }
        ReliableBroadcastProcess rbp = new ReliableBroadcastProcess(args[0]);
        rbp.start();
    }
}