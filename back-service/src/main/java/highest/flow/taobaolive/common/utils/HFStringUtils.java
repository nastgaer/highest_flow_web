package highest.flow.taobaolive.common.utils;

import org.apache.commons.lang3.StringUtils;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HFStringUtils {

    public static final String NUMBERS_REGEX = "-?[\\d]+(,\\d{3})*\\.?[\\d]*";

    /**
     * 获取字符串中介于prefix和suffix之间的字符串
     *
     * @param content 原字符串
     * @param prefix  前缀
     * @param suffix  后缀
     * @return 没有找到返回null。
     */
    public static String getMiddleText(String content, String prefix, String suffix) {
        return getMiddleText(content, prefix, suffix, true, true);
    }

    /**
     * 获取字符串中介于prefix和suffix之间的字符串
     *
     * @param content    原字符串
     * @param prefix     前缀
     * @param suffix     后缀
     * @param lazyPrefix 是否使用第一次找到的前缀作为截取前缀
     * @param lazySuffix 是否使用第一次找到的后缀作为截取后缀
     * @return 没有找到返回null。
     */
    public static String getMiddleText(String content, String prefix, String suffix, boolean lazyPrefix, boolean lazySuffix) {
        if (content != null) {
            if (prefix != null && suffix != null) {
                //截取前缀到后缀的字符串
                int prefixIndex = lazyPrefix ? content.indexOf(prefix) : content.lastIndexOf(prefix);
                if (prefixIndex >= 0) {
                    String tail = content.substring(prefixIndex + prefix.length());
                    int suffixIndex = lazySuffix ? tail.indexOf(suffix) : tail.lastIndexOf(suffix);
                    if (suffixIndex > 0) {
                        return tail.substring(0, suffixIndex);
                    }
                }
            } else if (prefix == null && suffix != null) {
                //截取后缀到末尾的字符串
                int suffixIndex = lazySuffix ? content.indexOf(suffix) : content.lastIndexOf(suffix);
                if (suffixIndex > 0) {
                    return content.substring(0, suffixIndex);
                }
            } else if (prefix != null && suffix == null) {
                //截取开头到前缀的字符串
                int prefixIndex = lazyPrefix ? content.indexOf(prefix) : content.lastIndexOf(prefix);
                if (prefixIndex >= 0) {
                    return content.substring(prefixIndex + prefix.length(), content.length());
                }
            }
        }
        return null;
    }

    /**
     * 获取字符串中介于prefix和suffix之间的字符串 然后拼接上prefix和suffix返回
     *
     * @param content    原字符串
     * @param prefix     前缀
     * @param suffix     后缀
     * @param lazyPrefix 是否使用第一次找到的前缀作为截取前缀
     * @param lazySuffix 是否使用第一次找到的后缀作为截取后缀
     * @return 没有找到返回null。
     */
    public static String getWholeText(String content, String prefix, String suffix, boolean lazyPrefix, boolean lazySuffix) {
        String middleText = getMiddleText(content, prefix, suffix, lazyPrefix, lazySuffix);
        if (middleText != null) {
            return new StringBuilder().append(StringUtils.defaultString(prefix)).append(middleText).append(StringUtils.defaultString(suffix)).toString();
        }
        return null;
    }

    /**
     * 获取字符串中介于prefix和suffix之间的字符串 然后拼接上prefix和suffix返回
     *
     * @param content 原字符串
     * @param prefix  前缀
     * @param suffix  后缀
     * @return 没有找到返回null。
     */
    public static String getWholeText(String content, String prefix, String suffix) {
        return getWholeText(content, prefix, suffix, true, true);
    }

    /**
     * 获取一个字符串中第一次出现的数字连续字符串
     *
     * @param text 原字符串
     * @return 没有找到返回null
     */
    public static String getFirstNumberFromText(String text) {
        return getFirstNumberFromText(text, true);
    }

    /**
     * 获取一个字符串中第一次出现的数字连续字符串
     *
     * @param text        原字符串
     * @param justNumbers 是否要求纯数字（没有小数点）
     * @return 没有找到返回null
     */
    public static String getFirstNumberFromText(String text, boolean justNumbers) {
        if (StringUtils.isNotBlank(text)) {
            String regex = justNumbers ? "\\d+" : NUMBERS_REGEX;
            Matcher numberMatcher = Pattern.compile(regex).matcher(text);
            if (numberMatcher.find()) {
                String number = numberMatcher.group();
                return number.replace(",", "");
            }
        }
        return "";
    }

    /**
     * 获取uuid
     *
     * @return uuid
     */
    public static String getUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

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

    /**
     * Using some super basic byte array &lt;-&gt; hex conversions so we don't have to rely on any
     * large Base64 libraries. Can be overridden if you like!
     *
     * @param bytes byte array to be converted
     * @return string containing hex values
     */
    public static String byteArrayToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte element : bytes) {
            int v = element & 0xff;
            if (v < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(v));
        }
        return sb.toString().toUpperCase(Locale.US);
    }

    /**
     * Converts hex values from strings to byte array
     *
     * @param hexString string of hex-encoded values
     * @return decoded byte array
     */
    public static byte[] hexStringToByteArray(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4) + Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }

    public static String convertToUtf8(String rawString) {
//        ByteBuffer buffer = StandardCharsets.UTF_8.encode(rawString);
//        String utf8Encoded = StandardCharsets.UTF_8.decode(buffer).toString();
//        return utf8Encoded;

        byte[] bytes = rawString.getBytes(StandardCharsets.UTF_8);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static String convertToGBK(String rawString) {
        byte[] bytes = rawString.getBytes(Charset.defaultCharset());
        return new String(bytes, Charset.defaultCharset());
    }

    public static String valueOf(Object obj) {
        return obj == null ? "" : String.valueOf(obj);
    }
}
