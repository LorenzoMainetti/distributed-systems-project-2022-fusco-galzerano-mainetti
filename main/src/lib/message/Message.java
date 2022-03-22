package lib.message;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public interface Message {
    char getType();

    InetAddress getSource();

    static Message parseString(String text, InetAddress sender) throws UnknownHostException {
        Scanner scanner = new Scanner(text);
        scanner.useDelimiter("|");
        String type = scanner.next();
        int sequenceNumber;
        switch (type) {
            case "T": // content message (normal)
                sequenceNumber = scanner.nextInt();
                String content = scanner.next();
                return new TextMessage(sender, content, sequenceNumber);
            case "N": // nack
                String stringTarget = scanner.next();
                InetAddress targetAddress = InetAddress.getByName(stringTarget);
                sequenceNumber = scanner.nextInt();
                return new NackMessage(sender, targetAddress, sequenceNumber);
            case "J": // join
                String addressString = scanner.next();
                sequenceNumber = scanner.nextInt();
                return new JoinMessage(sender, InetAddress.getByName(addressString), sequenceNumber);
            case "P": // ping
                // TODO: implement ping logic
            case "V": // viewchange
                int elements = scanner.nextInt();
                List<InetAddress> newView = new ArrayList<>();
                for (int i = 0; i < elements; ++i)
                    newView.add(InetAddress.getByName(scanner.next()));
                return new ViewChangeMessage(sender, newView);
            default:
                return new ErrorMessage(sender);
        }
    }

    String getTransmissionString();
}
