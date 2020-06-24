package highest.flow.taobaolive.common.http.httpclient.extended;

import org.apache.http.annotation.GuardedBy;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieIdentityComparator;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.impl.cookie.BasicClientCookie2;

import java.io.Serializable;
import java.util.*;

/**
 * 栅栏,不允许cookie时效超过1个小时。超过的减到1个小时。避免cookie过渡膨胀。适合无长期回话保持需求的场景。其实大多数情况都是这样的
 * Created by brucezee on 2017/1/10.
 */
public class BarrierCookieStore implements CookieStore, Serializable {

    private static final long serialVersionUID = -7581093305228232025L;

    @GuardedBy("this")
    private TreeSet<Cookie> cookies = null;

    private long maxExpiresMillis = 3600000;

    public BarrierCookieStore() {
        super();
        this.cookies = new TreeSet<Cookie>(new CookieIdentityComparator());
    }

    public BarrierCookieStore(long maxExpiresMillis) {
        super();
        this.maxExpiresMillis = maxExpiresMillis;
        this.cookies = new TreeSet<Cookie>(new CookieIdentityComparator());
    }

    /**
     * Adds an {@link Cookie HTTP cookie}, replacing any existing equivalent cookies.
     * If the given cookie has already expired it will not be added, but existing
     * values will still be removed.
     *
     * @param cookie the {@link Cookie cookie} to be added
     * @see #addCookies(Cookie[])
     */
    @Override
    public synchronized void addCookie(final Cookie cookie) {
        if (cookie != null) {
            // first remove any old cookie that is equivalent
            cookies.remove(cookie);
            Date now = new Date();
            if (!cookie.isExpired(now)) {
                Date targetExpiryDate = new Date(System.currentTimeMillis() + maxExpiresMillis);
                if (!cookie.isExpired(targetExpiryDate)) {
                    try {
                        if (cookie instanceof BasicClientCookie) {
                            ((BasicClientCookie) cookie).setExpiryDate(targetExpiryDate);
                        } else if (cookie instanceof BasicClientCookie2) {
                            ((BasicClientCookie2) cookie).setExpiryDate(targetExpiryDate);
                        }
                    } catch (Exception e) {
                    }
                }

                cookies.add(cookie);
            }
        }
    }

    /**
     * Adds an array of {@link Cookie HTTP cookies}. Cookies are added individually and
     * in the given array order. If any of the given cookies has already expired it will
     * not be added, but existing values will still be removed.
     *
     * @param cookies the {@link Cookie cookies} to be added
     * @see #addCookie(Cookie)
     */
    public synchronized void addCookies(final Cookie[] cookies) {
        if (cookies != null) {
            for (final Cookie cooky : cookies) {
                this.addCookie(cooky);
            }
        }
    }

    /**
     * Returns an immutable array of {@link Cookie cookies} that this HTTP
     * state currently contains.
     *
     * @return an array of {@link Cookie cookies}.
     */
    @Override
    public synchronized List<Cookie> getCookies() {
        //create defensive copy so it won't be concurrently modified
        return new ArrayList<Cookie>(cookies);
    }

    /**
     * Removes all of {@link Cookie cookies} in this HTTP state
     * that have expired by the specified {@link Date date}.
     *
     * @return true if any cookies were purged.
     * @see Cookie#isExpired(Date)
     */
    @Override
    public synchronized boolean clearExpired(final Date date) {
        if (date == null) {
            return false;
        }
        boolean removed = false;
        for (final Iterator<Cookie> it = cookies.iterator(); it.hasNext(); ) {
            if (it.next().isExpired(date)) {
                it.remove();
                removed = true;
            }
        }
        return removed;
    }

    /**
     * Clears all cookies.
     */
    @Override
    public synchronized void clear() {
        cookies.clear();
    }

    @Override
    public synchronized String toString() {
        return cookies.toString();
    }
}