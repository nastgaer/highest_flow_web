package highest.flow.taolive.xdata.http.cookie;

import highest.flow.taolive.xdata.http.Request;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;

import java.net.URI;
import java.util.List;

public class DefaultCookieStorePool extends CookieStorePool {

    private CookieStore cookieStore = null;

    public DefaultCookieStorePool(CookieStore cookieStore) {
        this.cookieStore = cookieStore;
    }

    @Override
    public CookieStore getCookieStore(Request request) {
        if (this.cookieStore != null) {
            try {
                URI uri = new URI(request.getUrl());

                CookieStore newCookieStore = new BasicCookieStore();

                List<Cookie> cookies = this.cookieStore.getCookies();
                for (Cookie cookie : cookies) {
                    if (cookie instanceof BasicClientCookie) {
                        ((BasicClientCookie)cookie).setDomain(uri.getHost());
                    }
                    newCookieStore.addCookie(cookie);
                }

                return newCookieStore;

            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }

        return cookieStore;
    }
}
