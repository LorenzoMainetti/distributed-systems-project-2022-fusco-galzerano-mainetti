package lib;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

/**
 This class uses the FIFO properties of the parent class to check
 for missing messages.
 */
public class RecoveringFifoBP extends FifoBroadcastProcess
{
    //This process maintains a list of all messages sent.
    private List<Message> messagesSent;

    //String used by other instances of this class to request missing
    // messages from **this** instance.
    private final String RESEND_REQUEST = "MISSING: " + super.procID + " ";

    /**
     Constructor.
     */
    public RecoveringFifoBP(String name) throws SocketException {
        super(name);
        messagesSent = new ArrayList<Message>();
    }

    /**
     Broadcasts a message with the specified text.  In addition, this adds this
     message to the list of messages sent out.  This list may later be used to
     resend any messages that other processes are missing.

     @see ReliableBroadcastProcess#broadcast(java.lang.String)
     */
    public void broadcast(String messageText)
    {
        Message m = new Message(messageText, super.procID, super.messageSentCount++);
        messagesSent.add(m);
        super.sendToAll(m);
    }

    /**
     Delivers the message to the system if no previous messages are missing. If
     there are missing messages, it will broadcast requests for those messages
     to be resent.

     @see ReliableBroadcastProcess#deliver(Message)
     */
    public void deliver(Message m)
    {
        //We expect 0 as the number of the next sequence, unless
        // we have already received messages from this process.
        int expectedMessageSeqNum = 0;
        if (super.messageSeqMap.containsKey(m.getSenderID()))
        {
            expectedMessageSeqNum = super.messageSeqMap.get(m.getSenderID());
        }

        //If we are missing a message, we request the missing messages
        // from the process that broadcast it.
        if (expectedMessageSeqNum != m.getSequenceNumber())
        {
            requestMessageResends(m, expectedMessageSeqNum);
        }

        //If another process has reported a missing message,
        //resend it to all processes.
        if (m.toString().startsWith(RESEND_REQUEST))
        {
            resendMessage(m);
        }

        super.deliver(m);
    }

    /**
     This method checks for messages that are missing and requests
     that they be resent.

     @param m  Last message received.
     @param expectedMessageSeqNum  Message sequence number expected.
     */
    private void requestMessageResends(Message m, int expectedMessageSeqNum)
    {
        int missingSeqNum = expectedMessageSeqNum;
        //
        while (missingSeqNum < m.getSequenceNumber())
        {
            Message messageToFind = new Message(null, m.getSenderID(), missingSeqNum);
            //If messages are missing, request a resend of those messages.
            if (!super.msgBag.contains(messageToFind))
            {
                super.broadcast("MISSING: " + m.getSenderID() + " " + missingSeqNum);
            }
            missingSeqNum++;
        }
    }

    /**
     Resends the specified message to all other processes.

     @param m  Message to resend.
     */
    private void resendMessage(Message m)
    {
        String numOfMissingMessage = m.toString().substring(RESEND_REQUEST.length());
        int n = Integer.parseInt(numOfMissingMessage);
        Message missingMessage = messagesSent.get(n);
        super.sendToAll(missingMessage);
    }

    /**
     Calls the start method for this class.
     */
    public static void main(String[] args) throws SocketException {
        if (args.length < 1)
        {
            System.out.println("Usage: java ImprovedFifoBP <process name>");
            System.exit(0);
        }
        RecoveringFifoBP ifbp = new RecoveringFifoBP(args[0]);
        ifbp.start();
    }

}
