package xdata;

import java.util.HashMap;
import java.util.Map;

public class ArgumentsParser {

    private static Map<String, String> options = new HashMap<>();

    public static void parse(String[] args) {
        for (String arg : args) {
            int index = arg.indexOf("=");
            String key = arg.substring(0, index).trim();
            String value = arg.substring(index + 1).trim();

            key = key.replace("--", "");
            value = value.replace("\"", "");
            options.put(key, value);
        }
    }

    public static boolean hasArgument(String key) {
        return options.containsKey(key);
    }

    public static String get(String key) {
        return (String) options.get(key);
    }
}
