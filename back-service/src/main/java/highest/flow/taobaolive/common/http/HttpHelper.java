package highest.flow.taobaolive.common.http;

import highest.flow.taobaolive.common.http.cookie.CookieStorePool;
import highest.flow.taobaolive.common.http.httpclient.DefaultHttpClientPool;
import highest.flow.taobaolive.common.http.httpclient.HttpClientExecutor;
import highest.flow.taobaolive.common.http.httpclient.HttpClientFactory;
import highest.flow.taobaolive.common.http.httpclient.HttpClientPool;
import highest.flow.taobaolive.common.http.httpclient.response.Response;
import highest.flow.taobaolive.common.http.proxy.HttpProxyPool;

public class HttpHelper {

    private static HttpClientPool httpClientPool;

    static {
        HttpHelper.httpClientPool = new DefaultHttpClientPool(HttpClientFactory.create());
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
