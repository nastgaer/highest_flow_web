package highest.flow.taobaolive.common.utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class HttpUtils {

    public static String doGet(String url) {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = null;
        try {
            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            return EntityUtils.toString(entity, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    public static String sendGet(String getUrl, Map<String, String> paraMap) {
        return sendGet(getUrl, paraMap, null);
    }

    public static String sendGet(String getUrl, Map<String, String> paraMap, Map<String, String> additionalHeaderMap) {
        if (paraMap == null) {
            paraMap = new HashMap<>();
        }
        paraMap = new TreeMap<>(paraMap);
        StringBuilder sb = new StringBuilder();
        paraMap.entrySet().stream().forEach(entry -> {
            sb.append(entry.getKey());
            sb.append("=");
            try {
                sb.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString()));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            sb.append("&");
        });
        getUrl = getUrl.contains("?") ? getUrl : getUrl + "?" + sb.toString();

        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(getUrl);
        CloseableHttpResponse response = null;
        try {
            if (additionalHeaderMap != null) {
                for (String header : additionalHeaderMap.keySet()) {
                    httpGet.setHeader(header, additionalHeaderMap.get(header));
                }
            }

            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            return EntityUtils.toString(entity, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String sendPost(String postUrl, Map<String, String> paraMap) {
        return sendPost(postUrl, paraMap, null);
    }

    public static String sendPost(String postUrl, Map<String, String> paraMap, Map<String, String> additionalHeaderMap) {
        /*
         * Create the POST request
         */
        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(postUrl);
        // Request parameters and other properties.
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        for (String key : paraMap.keySet()) {
            params.add(new BasicNameValuePair(key, paraMap.get(key).toString()));
        }
        try {
            if (additionalHeaderMap != null) {
                for (String header : additionalHeaderMap.keySet()) {
                    httpPost.setHeader(header, additionalHeaderMap.get(header));
                }
            }

            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            // writing error to Log
            e.printStackTrace();
        }
        /*
         * Execute the HTTP Request
         */
        try {
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity respEntity = response.getEntity();

            if (respEntity != null) {
                // EntityUtils to get the response content
                return EntityUtils.toString(respEntity);
            }
        } catch (ClientProtocolException e) {
            // writing exception to log
            e.printStackTrace();
        } catch (IOException e) {
            // writing exception to log
            e.printStackTrace();
        }
        return null;
    }

}
