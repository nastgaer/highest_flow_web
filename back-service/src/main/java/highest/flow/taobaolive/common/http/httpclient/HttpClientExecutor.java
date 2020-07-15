package highest.flow.taobaolive.common.http.httpclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import highest.flow.taobaolive.common.http.cookie.CookieStorePool;
import highest.flow.taobaolive.common.http.Request;
import highest.flow.taobaolive.common.http.SiteConfig;
import highest.flow.taobaolive.common.http.httpclient.response.Response;
import highest.flow.taobaolive.common.http.httpclient.response.ResponseFactory;
import highest.flow.taobaolive.common.http.proxy.HttpProxy;
import highest.flow.taobaolive.common.http.proxy.HttpProxyPool;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 请求执行器
 * Created by brucezee on 2017/1/6.
 */
public class HttpClientExecutor {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${http.inspect:false}")
    private boolean inspect;

    private HttpClientPool httpClientPool;
    private HttpProxyPool httpProxyPool;
    private CookieStorePool cookieStorePool;
    private SiteConfig siteConfig;
    private Request request;

    public HttpClientExecutor(HttpClientPool httpClientPool,
                              HttpProxyPool httpProxyPool,
                              CookieStorePool cookieStorePool,
                              SiteConfig siteConfig,
                              Request request) {
        this.httpClientPool = httpClientPool;
        this.httpProxyPool = httpProxyPool;
        this.cookieStorePool = cookieStorePool;
        this.siteConfig = siteConfig;
        this.request = request;
    }

    public <T> Response<T> execute() {
        HttpProxy httpProxy = getHttpProxyFromPool();
        CookieStore cookieStore = getCookieStoreFromPool();

        CloseableHttpClient httpClient = httpClientPool.getHttpClient(siteConfig, request);
        HttpUriRequest httpRequest = httpClientPool.createHttpUriRequest(siteConfig, request, createHttpHost(httpProxy));
        CloseableHttpResponse httpResponse = null;
        IOException executeException = null;
        try {
            if (inspect) {
                beforeExecuteRequest(httpRequest);
            }
            HttpContext httpContext = createHttpContext(httpProxy, cookieStore);
            httpResponse = httpClient.execute(httpRequest, httpContext);
        } catch (IOException e) {
            executeException = e;
        }

        Response<T> response = ResponseFactory.createResponse(
                request.getResponseType(), siteConfig.getCharset(request.getUrl()));
        response.setUrl(request.getUrl());

        response.handleHttpResponse(httpResponse, executeException);

        if (inspect) {
            afterExecuteRequest(response);
        }

        return response;
    }

    private void beforeExecuteRequest(HttpUriRequest request) {
        try {
            logger.debug("<<< Before Http Request");
            logger.debug("URL: " + request.getURI().toString());
            logger.debug("Method: " + request.getMethod());

            Map<String, String> mapHeaders = new HashMap<>();
            for (Header header : request.getAllHeaders()) {
                mapHeaders.put(header.getName(), header.getValue());
            }

            ObjectMapper objectMapper = new ObjectMapper();
            logger.debug("Headers: " + objectMapper.writeValueAsString(mapHeaders));

            if (request instanceof HttpEntityEnclosingRequestBase) {
                HttpEntity httpEntity = ((HttpEntityEnclosingRequestBase)request).getEntity();
                String content = IOUtils.toString(httpEntity.getContent(), StandardCharsets.UTF_8.name());
                logger.debug("Content: " + content);
            }

            logger.debug(">>> Before Http Request");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void afterExecuteRequest(Response response) {
        try {
            logger.debug(response.getResult().toString());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private HttpProxy getHttpProxyFromPool() {
        return httpProxyPool != null ? httpProxyPool.getProxy(request) : null;
    }

    private CookieStore getCookieStoreFromPool() {
        return cookieStorePool != null ? cookieStorePool.getCookieStore(request) : null;
    }

    private HttpHost createHttpHost(HttpProxy httpProxy) {
        return httpProxy != null ? new HttpHost(httpProxy.getHost(), httpProxy.getPort()) : null;
    }

    protected HttpContext createHttpContext(HttpProxy httpProxy, CookieStore cookieStore) {
        HttpContext httpContext = new HttpClientContext();

        if (cookieStore != null) {
            httpContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
        }

        if (httpProxy != null && StringUtils.isNotBlank(httpProxy.getUsername())) {
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(new AuthScope(httpProxy.getHost(), httpProxy.getPort()),
                    new UsernamePasswordCredentials(httpProxy.getUsername(), httpProxy.getPassword()));
            httpContext.setAttribute(HttpClientContext.CREDS_PROVIDER, credentialsProvider);
        }

        return httpContext;
    }
}
