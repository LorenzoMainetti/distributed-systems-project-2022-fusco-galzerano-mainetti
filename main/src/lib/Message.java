package lib;

/**
 Represents a single message, and handles the details of
 the made-up protocol that we are using.
 */
public class Message
{
    //text of the message.
    private String text;

    //These two fields tag the message and make it unique
    private String senderID;
    private int sequenceNum;

    //TODO check - do we need a pattern?
    //Pattern of our made up "protocol".
    private final static String pattern = "(.*?)\\|\\|sender:(.*?),seq:(\\d+)";

    /**
     Constructor for a new message.  At its creation, the message
     is tagged with the ID of the sender and the number that indicates
     the order that the message was sent.

     @param text
     @param senderID
     @param sequenceNum
     */
    public Message(String text, String senderID, int sequenceNum)
    {
        this.text = text;
        this.senderID = senderID;
        this.sequenceNum = sequenceNum;
    }

    /**
     This will take a transmissionString (in our made up protocol)
     and construct a new message object from it.

     @param receivedText  Text received from a broadcast message.
     */
    public static Message parseTransmissionString(String receivedText)
    {
        assert(receivedText.matches(pattern));
        String text = receivedText.replaceFirst(pattern, "$1");
        String ID = receivedText.replaceFirst(pattern, "$2");
        int seq = Integer.parseInt(receivedText.replaceFirst(pattern, "$3"));
        return new Message(text, ID, seq);
    }

    /**
     @return  ID of the original broadcaster of this message.
     */
    public String getSenderID()
    {
        return senderID;
    }

    /**
     @return  The sequence number of this message.
     */
    public int getSequenceNumber()
    {
        return sequenceNum;
    }

    /**
     Two messages are equal if the sender and sequence number are the same.

     @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o)
    {
        if (!(o instanceof Message)) return false;
        Message other = (Message)o;
        return this.senderID.equals(other.senderID) && this.sequenceNum==other.sequenceNum;
    }

    /**
     Returns the text of the message;
     @see java.lang.Object#toString()
     */
    public String toString()
    {
        return text;
    }

    /**
     Returns a transmission String for use in our protocol.
     @return
     */
    public String transmissionString()
    {
        return text + "||sender:" + senderID + ",seq:" + sequenceNum;
    }

    //TODO to be deleted
    /**
     Test main method for this class.  You can run this to verify
     that a message is not distorted after its reconstruction.

     @param args
     */
    public static void main(String [] args)
    {
        Message m1 = new Message("Hola.", "Alpha", 0);
        System.out.println(m1.transmissionString());
        Message m2 = Message.parseTransmissionString(m1.transmissionString());
        System.out.println("m1 eq? m2: " + m1.equals(m2));
    }
}
