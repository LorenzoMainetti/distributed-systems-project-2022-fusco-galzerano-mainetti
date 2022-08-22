package lib;

import lib.message.TextMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Settings {
    public static final boolean T_FAULT = true;
    public static final boolean P_FAULT = true;
    public static final boolean UNORDERED = true;


    public static final long SETUP_TIME = 10000;
    public static final long T_FREQUENCY = 5000;

    public static final long PING_PERIOD = 1000;
    public static final long PING_TIMEOUT = 5000;

    public static final boolean CAN_DROP_TEXT_MESSAGE = true;
    public static final double DROP_TEXT_MESSAGE_RATIO = 0.2;

    public static final List<TextMessage> unorderedMessagesList = new ArrayList<>();
    public static final double UNORDERED_CHANCE = 0.8;

    public static final Map<Integer, Integer> INITIAL_TIMEOUTS = new HashMap<>();
    public static final Map<Integer, Integer> N_MESSAGES = new HashMap<>();

    static {
        INITIAL_TIMEOUTS.put(2, 1000);
        INITIAL_TIMEOUTS.put(3, 10000);
        INITIAL_TIMEOUTS.put(4, 20000);

        N_MESSAGES.put(2, 20);
        N_MESSAGES.put(3, 15);
        N_MESSAGES.put(4, 10);
    }
}
