package app;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class Demo {
    public static void main(String[] args) throws UnknownHostException {
        List<InetAddress> l = new ArrayList<>();

        l.add(InetAddress.getByName("0.0.0.0"));
        System.out.println(l.size());
        System.out.println(InetAddress.getByName("0.0.0.0").equals(InetAddress.getByName("0.0.0.0")));

        l.remove(InetAddress.getByName("0.0.0.0"));
        System.out.println(l.size());
    }
}
