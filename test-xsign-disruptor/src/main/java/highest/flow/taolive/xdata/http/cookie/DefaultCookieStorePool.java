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
                String host = uri.getHost();

                CookieStore newCookieStore = new BasicCookieStore();

                List<Cookie> cookies = this.cookieStore.getCookies();
                for (Cookie cookie : cookies) {
                    Cookie cloneCookie = cookie;
                    if (cookie instanceof BasicClientCookie) {
                        BasicClientCookie basicClientCookie = (BasicClientCookie) cookie;
                        if (host.toLowerCase().indexOf(basicClientCookie.getDomain()) >= 0) {
                            cloneCookie = (Cookie) basicClientCookie.clone();
                            ((BasicClientCookie)cloneCookie).setDomain(host);

                        } else {
                            continue;
                        }
                    }
                    newCookieStore.addCookie(cloneCookie);
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
