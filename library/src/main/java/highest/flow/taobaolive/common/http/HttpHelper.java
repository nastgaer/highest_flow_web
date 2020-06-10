package highest.flow.taobaolive.common.http;

import highest.flow.taobaolive.common.config.Config;
import highest.flow.taobaolive.common.http.response.Response;
import highest.flow.taobaolive.common.http.response.ResponseFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

public class HttpHelper {

    private CloseableHttpClient getHttpClient(Request request) {
        CloseableHttpClient httpClient = HttpClients.custom()
                .setUserAgent(request.getUserAgent())
                .setDefaultCookieStore(new BasicCookieStore())
                .build();

        return httpClient;
    }

    private RequestBuilder createRequestBuilder(Request request) {
        RequestBuilder requestBuilder = RequestBuilder.create(request.getMethod());
        requestBuilder.setCharset(Charset.forName(Config.DEFAULT_CHARSET));
        requestBuilder.setUri(request.getUrl());

        Map<String, String> parameters = request.getParameters();
        if (parameters != null && !parameters.isEmpty()) {
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                requestBuilder.addParameter(entry.getKey(), StringUtils.defaultString(entry.getValue()));
            }
        }

        Map<String, String> requestHeaders = request.getHeaders();
        if (requestHeaders != null && !requestHeaders.isEmpty()) {
            for (Map.Entry<String, String> entry : requestHeaders.entrySet()) {
                requestBuilder.addHeader(entry.getKey(), StringUtils.defaultString(entry.getValue()));
            }
        }

        return requestBuilder;
    }

    private HttpUriRequest createHttpUriRequest(Request request) {
        return createRequestBuilder(request).build();
    }

    public <T> Response<T> execute(Request request) {
        CloseableHttpClient httpClient = getHttpClient(request);
        HttpUriRequest httpUriRequest = createHttpUriRequest(request);
        HttpClientContext httpContext = new HttpClientContext();

        CloseableHttpResponse httpResponse = null;
        IOException executeException = null;

        try {
            if (request.getCookieStore() != null) {
                httpContext.setCookieStore(request.getCookieStore());
            }

            httpResponse = httpClient.execute(httpUriRequest, httpContext);

        } catch (IOException e) {
            executeException = e;
        }

        Response<T> response = ResponseFactory.createResponse(
                request.getResponseType(), Config.DEFAULT_CHARSET);

        response.handleHttpResponse(httpResponse, executeException);
        response.setCookieStore(httpContext.getCookieStore());
        return response;
    }
}
