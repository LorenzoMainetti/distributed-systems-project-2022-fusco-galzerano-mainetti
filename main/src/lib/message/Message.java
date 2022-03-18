package lib.message;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

public interface Message {
    char getType();

    static Message parseString(String text, InetAddress sender) throws UnknownHostException {
        Scanner scanner = new Scanner(text);
        scanner.useDelimiter("|");
        String type = scanner.next();
        int sequenceNumber;
        switch (type) {
            case "M": // content message (normal)
                sequenceNumber = scanner.nextInt();
                String content = scanner.next();
                return new TextMessage(content, sender, sequenceNumber);
            case "N": // nack
                String stringTarget = scanner.next();
                InetAddress targetAddress = InetAddress.getByName(stringTarget);
                sequenceNumber = scanner.nextInt();
                return new NackMessage(sender, targetAddress, sequenceNumber);
            case "J": // join
                String addressString = scanner.next();
                sequenceNumber = scanner.nextInt();
                return new JoinMessage(InetAddress.getByName(addressString), sequenceNumber);
            case "P": // ping
                // TODO: implement ping logic
            default:
                return new ErrorMessage();
        }
    }

    String getTransmissionString();
}
