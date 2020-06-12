package highest.flow.taobaolive.taobao.service.impl;

import highest.flow.taobaolive.common.http.HttpHelper;
import highest.flow.taobaolive.common.http.SiteConfig;
import highest.flow.taobaolive.common.http.httpclient.DefaultHttpClientPool;
import highest.flow.taobaolive.common.http.httpclient.HttpClientExecutor;
import highest.flow.taobaolive.common.http.httpclient.HttpClientFactory;
import highest.flow.taobaolive.common.http.Request;
import highest.flow.taobaolive.common.http.ResponseType;
import highest.flow.taobaolive.common.http.httpclient.HttpClientPool;
import highest.flow.taobaolive.common.http.httpclient.response.Response;
import highest.flow.taobaolive.common.http.proxy.DefaultHttpProxyPool;
import highest.flow.taobaolive.common.utils.CryptoUtils;
import highest.flow.taobaolive.common.utils.HFStringUtils;
import highest.flow.taobaolive.taobao.entity.XHeader;
import highest.flow.taobaolive.taobao.service.XSignService;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;

@Service("xSignService")
public class XSignServiceImpl implements XSignService {

    private int[] availablePorts = new int[]{
            59316, 58119, 58114, 58120
    };
    private int port2 = 59316;

    private String prepareXSign1(XHeader xHeader, int port) {
        try {
            xHeader.setEncoded(true);
            String aes = CryptoUtils.MD5(xHeader.getData());
            String plain = xHeader.getUtdid() + "%26" + xHeader.getUid() + "%26%26" + xHeader.getAppkey() + "%26" + aes + "%26" +
                    xHeader.getShortTimestamp() + "%26" + xHeader.getSubUrl() + "%26" + xHeader.getUrlVer() + "%26" +
                    xHeader.getSid() + "%26" + xHeader.getTtid() + "%26" + xHeader.getDevid() + "%26" +
                    xHeader.getLocation2() + "%26" + xHeader.getLocation1() + "%26" + xHeader.getFeatures();

            String timeMD5 = CryptoUtils.MD5(String.valueOf(xHeader.getLongTimestamp()));

            String url = "http://1.192.134.231:" + port + "/xdata?data=" + plain + "&apiKey=&t=&apiSign=" + URLEncoder.encode(timeMD5);

            SiteConfig siteConfig = new SiteConfig()
                    .setUserAgent("MTOPSDK/3.1.1.7 (Android;5.1.1)")
                    .addHeaders(xHeader.getHeaders())
                    .setConnectTimeout(30000)
                    .setSocketTimeout(45000);

            Response<String> response = HttpHelper.execute(siteConfig, new Request("GET", url, ResponseType.TEXT));
            if (response.getStatusCode() != HttpStatus.SC_OK) {
                return null;
            }

            String respText = response.getResult();
            respText = StringUtils.strip(respText, "\"\r\n");

            if (respText.startsWith("ab2")) {
                return respText;
            }
            return null;

        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
        return null;
    }

    @Override
    public String sign1(XHeader xHeader) {
        String xsign = prepareXSign1(xHeader, port2);
        if (!HFStringUtils.isNullOrEmpty(xsign)) {
            return xsign;
        }

        for (int port : availablePorts) {
            xsign = prepareXSign1(xHeader, port);
            if (!HFStringUtils.isNullOrEmpty(xsign)) {
                port2 = port;
                return xsign;
            }
        }
        return null;
    }

    @Override
    public String sign2(XHeader xHeader) {
        try {
            xHeader.setEncoded(false);
            String aes = CryptoUtils.MD5(xHeader.getData());
            String plain = xHeader.getUtdid() + "&" + xHeader.getUid() + "&&" + xHeader.getAppkey() + "&" + aes + "&" +
                    xHeader.getShortTimestamp() + "&" + xHeader.getSubUrl() + "&" + xHeader.getUrlVer() + "&" +
                    xHeader.getSid() + "&" + xHeader.getTtid() + "&" + xHeader.getDevid() + "&" +
                    xHeader.getLocation2() + "&" + xHeader.getLocation1() + "&" + xHeader.getFeatures();

            String url = "http://39.100.74.215:2345/x-sign.php?" + plain;

            SiteConfig siteConfig = new SiteConfig()
                    .setUserAgent("MTOPSDK/3.1.1.7 (Android;5.1.1)")
                    .addHeaders(xHeader.getHeaders())
                    .setConnectTimeout(30000)
                    .setSocketTimeout(45000);

            Response<String> response = HttpHelper.execute(siteConfig, new Request("GET", url, ResponseType.TEXT));
            if (response.getStatusCode() != HttpStatus.SC_OK) {
                return null;
            }

            String respText = response.getResult();

            if (respText == null) {
                return null;
            }

            respText = StringUtils.strip(respText, "\"\r\n");

            if (respText.startsWith("ab2")) {
                return respText;
            }
            return null;

        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
        return null;
    }

    @Override
    public String sign(XHeader xHeader) {
        String xsign = sign1(xHeader);
        if (!HFStringUtils.isNullOrEmpty(xsign)) {
            return xsign;
        }

        return sign2(xHeader);
    }
}
