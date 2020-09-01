package highest.flow.taobaolive.common.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class CommonUtils {

    public static int randomInt(int min, int max) {
        Random random = new Random();
        int randnum = random.nextInt((max - min) + 1) + min;
        return randnum;
    }

    public static String randomAlphabetic(int length) {
        String charset = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        String ret = "";
        for (int idx = 0; idx < length; idx++)
        {
            int rand = randomInt(0, charset.length() - 1);
            ret = ret + charset.charAt(rand);
        }
        return ret;
    }

    public static String randomNumeric(int length) {
        String charset = "0123456789";
        String ret = "";
        for (int idx = 0; idx < length; idx++)
        {
            int rand = randomInt(0, charset.length() - 1);
            ret = ret + charset.charAt(rand);
        }
        return ret;
    }

    public static Date timestampToDate(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        return calendar.getTime();
    }

    public static long dateToTimestamp(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.getTimeInMillis();
    }

    public static Date addDays(Date date, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, days);
        return calendar.getTime();
    }

    public static Date addHours(Date date, int hours) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR_OF_DAY, hours);
        return calendar.getTime();
    }

    public static Date addMinutes(Date date, int minutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, minutes);
        return calendar.getTime();
    }
}
