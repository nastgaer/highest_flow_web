package highest.flow.taobaolive.taobao.utils;

import highest.flow.taobaolive.common.utils.CommonUtils;

public class DeviceUtils {

    public static String generateUtdid() {
        return CommonUtils.randomAlphabetic("6e7lRDR6oED7edYhrW48Tzd4".length());
    }

    public static String generateDevid() {
        return CommonUtils.randomAlphabetic("Y7Z5ZBp1K28Fcn4c7yXIeWQcfk0sqV4q0PRkcu8yXI0D".length());
    }

    public static String generateSid() {
        return CommonUtils.randomAlphabetic("7026e4c58f631c91aa58f3150410cd09".length()).toLowerCase();
    }

    public static String generateIMEI() {
        return "86516602" + CommonUtils.randomNumeric(7);
    }

    public static String generatePhoneC3() {
        return "46006" + CommonUtils.randomNumeric("9045857802".length());
    }

    public static String generateMAC() {
        // 9c:c3:b0:15:c7:4b

        String mac = "";
        for (int idx = 0; idx < 6; idx++)
        {
            mac += ":" + CommonUtils.randomAlphabetic(2);
        }

        return mac.substring(1).toLowerCase();
    }
}