package highest.flow.taobaolive.common.utils;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    public static Date asDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    public static Date asDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDate asLocalDate(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static LocalDateTime asLocalDateTime(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static Date parseDateStr(String dateStr) {
//        String [] patterns = new String[] {
//                "EEE, dd MMM yyyy HH:mm:ss",
//                "EEE, yy MMM dd HH:mm:ss",
//                "yyyy MM dd HH:mm:ss"
//        };
//
//        dateStr = dateStr.replace("/", "-")
//                .replace("-", " ")
//                .replace("GMT", "").trim();
//
//        for (String pattern : patterns) {
//            try {
//                SimpleDateFormat sdf = new SimpleDateFormat(pattern);
//                return sdf.parse(dateStr);
//
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        }

        if (dateStr.indexOf("GMT") >= 0) {
            String [] patterns = new String[] {
                    "EEE, dd MMM yyyy HH:mm:ss",
                    "EEE, dd MMM yy HH:mm:ss"
            };

            dateStr = dateStr.replace("/", "-")
                    .replace("-", " ")
                    .replace("GMT", "").trim();

            for (String pattern : patterns) {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH);
                    LocalDateTime localDateTime = LocalDateTime.parse(dateStr.replace(" GMT", ""), formatter);

                    return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());

                } catch (Exception ex) {
//                    ex.printStackTrace();
                }
            }

        } else {
            dateStr = dateStr.replace("/", "-")
                    .replace("-", " ")
                    .replace("GMT", "").trim();

            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy MM dd HH:mm:ss");
                return sdf.parse(dateStr);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return null;
    }
}
