package highest.flow.taolive.xdata.http;

import highest.flow.taolive.xdata.http.cookie.CookieStorePool;
import highest.flow.taolive.xdata.http.httpclient.DefaultHttpClientPool;
import highest.flow.taolive.xdata.http.httpclient.HttpClientExecutor;
import highest.flow.taolive.xdata.http.httpclient.HttpClientFactory;
import highest.flow.taolive.xdata.http.httpclient.HttpClientPool;
import highest.flow.taolive.xdata.http.httpclient.response.Response;
import highest.flow.taolive.xdata.http.proxy.HttpProxyPool;

public class HttpHelper {

    private static HttpClientPool httpClientPool;

    static {
        HttpHelper.httpClientPool = new DefaultHttpClientPool(new HttpClientFactory());
    }

    public static <T> Response<T> execute(SiteConfig siteConfig, Request request) {
        HttpClientExecutor executor = new HttpClientExecutor(
                httpClientPool,
                null,
                null,
                siteConfig,
                request);

        return executor.execute();
    }

    public static <T> Response<T> execute(SiteConfig siteConfig, Request request, CookieStorePool cookieStorePool) {
        HttpClientExecutor executor = new HttpClientExecutor(
                httpClientPool,
                null,
                cookieStorePool,
                siteConfig,
                request);

        return executor.execute();
    }

    public static <T> Response<T> execute(SiteConfig siteConfig, Request request, HttpProxyPool httpProxyPool, CookieStorePool cookieStorePool) {
        HttpClientExecutor executor = new HttpClientExecutor(
                httpClientPool,
                httpProxyPool,
                cookieStorePool,
                siteConfig,
                request);

        return executor.execute();
    }
}
