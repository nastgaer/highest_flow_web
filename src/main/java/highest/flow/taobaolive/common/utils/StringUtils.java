package highest.flow.taobaolive.common.utils;

public class StringUtils {

    public static boolean isNullOrEmpty(String text) {
        if (text == null || text.length() < 1) {
            return true;
        }
        return false;
    }

    public static String trim(String text, char[] chars) {
        int startPos = 0, endPos = text.length() - 1;
        for (int idx = 0; idx < text.length(); idx++) {
            char c = text.charAt(idx);
            boolean found = false;
            for (int idx1 = 0; idx1 < chars.length; idx1++) {
                if (c == chars[idx1]) {
                    found = true;
                }
            }
            if (found) {
                startPos = idx;
            } else {
                break;
            }
        }
        for (int idx = text.length() - 1; idx >= 0; idx--) {
            char c = text.charAt(idx);
            boolean found = false;
            for (int idx1 = 0; idx1 < chars.length; idx1++) {
                if (c == chars[idx1]) {
                    found = true;
                }
            }
            if (found) {
                endPos = idx;
            } else {
                break;
            }
        }
        return text.substring(startPos, endPos);
    }
}
