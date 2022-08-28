package lib;

import lib.message.TextMessage;

import java.util.ArrayList;
import java.util.List;

public class Test {
    public static boolean dropNextMessage = false;
    public static double DROP_TEXT_MESSAGE_RATIO = 0;

    public static List<TextMessage> unorderedMessagesList = new ArrayList<>();

    public static boolean UNORDERED = false;
    public static double UNORDERED_CHANCE = 1;
}
