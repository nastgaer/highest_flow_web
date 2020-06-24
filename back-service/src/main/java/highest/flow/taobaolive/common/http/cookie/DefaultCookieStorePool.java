package highest.flow.taobaolive.common.http.cookie;

import highest.flow.taobaolive.common.http.Request;
import org.apache.http.client.CookieStore;

public class DefaultCookieStorePool extends CookieStorePool {

    private CookieStore cookieStore = null;

    public DefaultCookieStorePool(CookieStore cookieStore) {
        this.cookieStore = cookieStore;
    }

    @Override
    public CookieStore getCookieStore(Request request) {
        return cookieStore;
    }
}
