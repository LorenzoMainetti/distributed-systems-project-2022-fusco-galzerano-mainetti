package lib;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 This class represents a FIFO Broadcast Process, built on top of
 Reliable Broadcast.  The broadcast algorithm is unchanged, but
 the delivery algorithm is overridden.
 */
public class FifoBroadcastProcess extends ReliableBroadcastProcess
{
    //Messages that have been R-delivered (meaning that they were delivered
    // by Reliable Broadcast) but have not yet been delayed by this algorithm.
    protected List<Message> msgBag;

    //Records the sequence number of the next message that
    // we are expecting to see from the corresponding process.
    protected Map<String,Integer> messageSeqMap;

    /**
     Constructor
     */
    public FifoBroadcastProcess(String ID) throws SocketException {
        super(ID);
        msgBag = new ArrayList<Message>();
        messageSeqMap = new HashMap<String,Integer>();
    }

    /**
     This represents the delivery of a message from Reliable
     Broadcast (aka R-delivery).  This will not be delivered
     to the system by a FIFO process (aka F-Delivery) until
     all previous messages from the sending process have
     been received.

     @see ReliableBroadcastProcess#deliver(Message)
     */
    public void deliver(Message m)
    {
        //Process is put in a queue
        msgBag.add(m);

        //Unlike the text's algorithm, we only keep track of the number
        // of messages sent from a process from the first time that we
        // receive a broadcast message from it.  This frees us from
        // having to keep a specific list of which processes this
        // one may communicate with.
        if (!messageSeqMap.containsKey(m.getSenderID()))
        {
            messageSeqMap.put(m.getSenderID(), 0);
        }

        //After receiving a new message, we deliver all
        // messages to the system that we now can.
        fDeliverAllPossibleMessages(m.getSenderID());
    }

    /**
     If a message is delivered, and any messages were
     queued up waiting for its arrival, this method
     will deliver all of them.
     */
    private void fDeliverAllPossibleMessages(String sender)
    {
        boolean newDelivery = false;
        for (Iterator<Message> iter = msgBag.iterator(); iter.hasNext(); )
        {
            Message m = iter.next();
            int expected = messageSeqMap.get(m.getSenderID());
            if (m.getSequenceNumber()==expected && m.getSenderID().equals(sender))
            {
                fDeliver(m);
                messageSeqMap.put(m.getSenderID(), expected+1);
                iter.remove();
                newDelivery = true;
            }
        }
        //If a message was delivered in the last pass, we must cycle
        // through them again to ensure that no messages waiting for
        // THAT message's delivery are still waiting needlessly.
        if (newDelivery) fDeliverAllPossibleMessages(sender);
    }

    /**
     When this method is called, the message has finally
     been F-delivered to the process.
     */
    protected void fDeliver(Message m)
    {
        super.deliver(m);
    }

    /**
     Calls the start method for this class.
     */
    public static void main(String[] args) throws SocketException {
        if (args.length < 1)
        {
            System.out.println("Usage: java FifoBroadcastProcess <process name>");
            System.exit(0);
        }
        FifoBroadcastProcess fbp = new FifoBroadcastProcess(args[0]);
        fbp.start();
    }
}
