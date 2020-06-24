package highest.flow.taobaolive.common.http.httpclient;

import highest.flow.taobaolive.common.http.HttpUtils;
import highest.flow.taobaolive.common.http.Request;
import highest.flow.taobaolive.common.http.SiteConfig;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 默认HttpClient池
 * Created by brucezee on 2017/3/20.
 */
public class DefaultHttpClientPool implements HttpClientPool {
    private HttpClientFactory factory = null;
    private Map<String, CloseableHttpClient> httpClients = null;

    public DefaultHttpClientPool(HttpClientFactory factory) {
        this.factory = factory;
        this.httpClients = new HashMap<String, CloseableHttpClient>();
    }

    @Override
    public CloseableHttpClient getHttpClient(SiteConfig siteConfig, Request request) {
        String host = getHttpClientCacheKey(siteConfig, request);
        CloseableHttpClient httpClient = httpClients.get(host);
        if (httpClient == null) {
            synchronized (this) {
                httpClient = httpClients.get(host);
                if (httpClient == null) {
                    httpClient = factory.createHttpClient(siteConfig);
                    httpClients.put(host, httpClient);
                }
            }
        }
        return httpClient;
    }

    protected String getHttpClientCacheKey(SiteConfig siteConfig, Request request) {
        return HttpUtils.getUrlHost(request.getUrl());
    }

    public HttpClientFactory getFactory() {
        return factory;
    }

    @Override
    public HttpUriRequest createHttpUriRequest(SiteConfig siteConfig, Request request, HttpHost proxy) {
        return factory.createHttpUriRequest(siteConfig, request, proxy);
    }

    @Override
    public void close() throws IOException {
        for (Map.Entry<String, CloseableHttpClient> entry : httpClients.entrySet()) {
            try {
                entry.getValue().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
