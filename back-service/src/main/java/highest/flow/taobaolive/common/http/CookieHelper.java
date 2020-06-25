package highest.flow.taobaolive.common.http;

import highest.flow.taobaolive.common.utils.HFStringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.http.cookie.*;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.impl.cookie.BrowserCompatSpec;
import org.apache.http.message.BasicHeader;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.web.util.WebUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CookieHelper {

    public static List<Cookie> parseCookieHeaders(String url, List<String> cookieHeaders) {
        try {
            CookieSpec cookieSpec = new BrowserCompatSpec();
            URI uri = new URI(url);

            List<Cookie> lstCookies = new ArrayList<>();

            int port = (uri.getPort() < 0) ? 80 : uri.getPort();
            boolean secure = "https".equals(uri.getScheme());
            CookieOrigin origin = new CookieOrigin(uri.getHost(), port,
                    uri.getPath(), secure);

            for (String cookieHeader : cookieHeaders) {
                BasicHeader header = new BasicHeader(SM.SET_COOKIE, cookieHeader);
                try {
                    lstCookies.addAll(cookieSpec.parse(header, origin));
                } catch (MalformedCookieException ex) {
                    ex.printStackTrace();
                }
            }
            return lstCookies;

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String encodeCookie(Cookie cookie) {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(os);

            oos.writeObject(cookie.getName());
            oos.writeObject(cookie.getValue());
            oos.writeObject(cookie.getComment());
            oos.writeObject(cookie.getDomain());
            oos.writeObject(cookie.getExpiryDate());
            oos.writeObject(cookie.getPath());
            oos.writeInt(cookie.getVersion());
            oos.writeBoolean(cookie.isSecure());

            String cookieSerialize = HFStringUtils.byteArrayToHexString(os.toByteArray());
            oos.close();

            return cookieSerialize;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static Cookie decodeCookie(String cookieBytesHex) {
        try {
            byte[] bytes = HFStringUtils.hexStringToByteArray(cookieBytesHex);
            ByteArrayInputStream is = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(is);

            String name = (String) ois.readObject();
            String value = (String) ois.readObject();

            BasicClientCookie cookie = new BasicClientCookie(name, value);
            cookie.setComment((String) ois.readObject());
            cookie.setDomain((String) ois.readObject());
            cookie.setExpiryDate((Date) ois.readObject());
            cookie.setPath((String) ois.readObject());
            cookie.setVersion(ois.readInt());
            cookie.setSecure(ois.readBoolean());

            ois.close();
            return cookie;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String toString(Cookie cookie) {
        try {
            String cookieHeader = "";

            cookieHeader += cookie.getName() + "=" + cookie.getValue() + ";";
            cookieHeader += "Domain=" + cookie.getDomain() + ";";
            cookieHeader += "Path=" + cookie.getPath() + ";";
            if (cookie.getExpiryDate() != null) {
                cookieHeader += "Expires=" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cookie.getExpiryDate()) + ";";
            }
            if (cookie.isSecure()) {
                cookieHeader += "Secure;";
            }
            return cookieHeader;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

    public static Cookie parseString(String rawCookie) {
        try {
            String[] rawCookieParams = rawCookie.split(";");

            int pos = rawCookieParams[0].indexOf('=');
            if (pos < 0) {
                return null;
            }

            String[] rawCookieNameAndValue = new String[] {
                    rawCookieParams[0].substring(0, pos),
                    rawCookieParams[0].substring(pos + 1)
            };

            String cookieName = rawCookieNameAndValue[0].trim();
            String cookieValue = rawCookieNameAndValue[1].trim();
            BasicClientCookie cookie = new BasicClientCookie(cookieName, cookieValue);
            for (int i = 1; i < rawCookieParams.length; i++) {
                String params = rawCookieParams[i].trim();
                pos = params.indexOf('=');
                if (pos < 0) {
                    continue;
                }
                String rawCookieParamNameAndValue[] = new String[] {
                        params.substring(0, pos),
                        params.substring(pos + 1)
                };

                String paramName = rawCookieParamNameAndValue[0].trim();

                if (paramName.equalsIgnoreCase("secure")) {
                    cookie.setSecure(true);
                } else {
                    if (rawCookieParamNameAndValue.length != 2) {
                        return null;
                    }

                    String paramValue = rawCookieParamNameAndValue[1].trim();

                    if (paramName.equalsIgnoreCase("expires")) {
                        if (paramValue.indexOf("GMT") >= 0) {
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, dd-MMM-yyyy HH:mm:ss", Locale.ENGLISH);
                            LocalDateTime localDateTime = LocalDateTime.parse(paramValue.replace(" GMT", ""), formatter);

                            Date expiryDate = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
                            cookie.setExpiryDate(expiryDate);
                        } else {
                            Date expiryDate = DateFormat.getDateTimeInstance().parse(paramValue);
                            cookie.setExpiryDate(expiryDate);
                        }
                    } else if (paramName.equalsIgnoreCase("max-age")) {
                        long maxAge = Long.parseLong(paramValue);
                        Date expiryDate = new Date(System.currentTimeMillis() + maxAge);
                        cookie.setExpiryDate(expiryDate);
                    } else if (paramName.equalsIgnoreCase("domain")) {
                        cookie.setDomain(paramValue);
                    } else if (paramName.equalsIgnoreCase("path")) {
                        cookie.setPath(paramValue);
                    } else if (paramName.equalsIgnoreCase("comment")) {
                        cookie.setPath(paramValue);
                    } else {
                        return null;
                    }
                }
            }

            return cookie;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
