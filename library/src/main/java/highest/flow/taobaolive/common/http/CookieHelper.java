package highest.flow.taobaolive.common.http;

import highest.flow.taobaolive.common.utils.HFStringUtils;
import org.apache.http.cookie.*;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.impl.cookie.BrowserCompatSpec;
import org.apache.http.message.BasicHeader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
}
