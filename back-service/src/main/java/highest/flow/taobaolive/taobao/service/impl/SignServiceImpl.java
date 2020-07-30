package highest.flow.taobaolive.taobao.service.impl;

import highest.flow.taobaolive.common.http.HttpHelper;
import highest.flow.taobaolive.common.http.SiteConfig;
import highest.flow.taobaolive.common.http.Request;
import highest.flow.taobaolive.common.http.ResponseType;
import highest.flow.taobaolive.common.http.httpclient.response.Response;
import highest.flow.taobaolive.common.utils.CryptoUtils;
import highest.flow.taobaolive.common.utils.HFStringUtils;
import highest.flow.taobaolive.taobao.entity.H5Header;
import highest.flow.taobaolive.taobao.entity.XHeader;
import highest.flow.taobaolive.taobao.service.SignService;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;

@Service("signService")
public class SignServiceImpl implements SignService {

    private int mode = 1;

    @Override
    public String xsign1(XHeader xHeader) {
        try {
            xHeader.setEncoded(true);
            String aes = CryptoUtils.MD5(xHeader.getData());
            String plain = xHeader.getUtdid() + "%26" + xHeader.getUid() + "%26%26" + xHeader.getAppkey() + "%26" + aes + "%26" +
                    xHeader.getShortTimestamp() + "%26" + xHeader.getSubUrl() + "%26" + xHeader.getUrlVer() + "%26" +
                    xHeader.getSid() + "%26" + xHeader.getTtid() + "%26" + xHeader.getDevid() + "%26" +
                    xHeader.getLocation2() + "%26" + xHeader.getLocation1() + "%26" + xHeader.getFeatures();

            String timeMD5 = CryptoUtils.MD5(String.valueOf(xHeader.getLongTimestamp()));

            String url = "http://1.192.134.231:59316/xdata?data=" + plain + "&apiKey=&t=&apiSign=" + URLEncoder.encode(timeMD5);

            SiteConfig siteConfig = new SiteConfig()
                    .setUserAgent("MTOPSDK/3.1.1.7 (Android;5.1.1)")
                    .setConnectTimeout(10000)
                    .setSocketTimeout(10000);

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
    public String xsign2(XHeader xHeader) {
        try {
            xHeader.setEncoded(false);
            String aes = CryptoUtils.MD5(xHeader.getData());
            String plain = xHeader.getUtdid() + "&" + xHeader.getUid() + "&&" + xHeader.getAppkey() + "&" + aes + "&" +
                    xHeader.getShortTimestamp() + "&" + xHeader.getSubUrl() + "&" + xHeader.getUrlVer() + "&" +
                    xHeader.getSid() + "&" + xHeader.getTtid() + "&" + xHeader.getDevid() + "&" +
                    xHeader.getLocation2() + "&" + xHeader.getLocation1() + "&" + xHeader.getFeatures();

            String url = "http://39.100.74.215:2345/x-xsign.php?" + plain;

            SiteConfig siteConfig = new SiteConfig()
                    .setUserAgent("MTOPSDK/3.1.1.7 (Android;5.1.1)")
                    .addHeaders(xHeader.getHeaders())
                    .setConnectTimeout(10000)
                    .setSocketTimeout(10000);

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
    public String xsign3(XHeader xHeader) {
        try {
            xHeader.setEncoded(false);
            String aes = CryptoUtils.MD5(xHeader.getData());
            String plain = xHeader.getUtdid() + "&" + xHeader.getUid() + "&&" + xHeader.getAppkey() + "&" + aes + "&" +
                    xHeader.getShortTimestamp() + "&" + xHeader.getSubUrl() + "&" + xHeader.getUrlVer() + "&" +
                    xHeader.getSid() + "&" + xHeader.getTtid() + "&" + xHeader.getDevid() + "&" +
                    xHeader.getLocation2() + "&" + xHeader.getLocation1() + "&" + xHeader.getFeatures();

            // 39.96.180.58
            String url = "http://yunta0.320.io/xdata?data=" + URLEncoder.encode(plain);

            SiteConfig siteConfig = new SiteConfig()
                    .setUserAgent("MTOPSDK/3.1.1.7 (Android;5.1.1)")
                    .addHeaders(xHeader.getHeaders())
                    .setConnectTimeout(10000)
                    .setSocketTimeout(10000);

            Response<String> response = HttpHelper.execute(siteConfig, new Request("GET", url, ResponseType.TEXT));
            if (response.getStatusCode() != HttpStatus.SC_OK) {
                return null;
            }

            String respText = response.getResult();

            if (respText == null) {
                return null;
            }

            respText = StringUtils.strip(respText, "\"\r\n\0");

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
    public String xsign(XHeader xHeader) {
        if (mode == 3) {
            String xsign = xsign3(xHeader);
            if (!HFStringUtils.isNullOrEmpty(xsign)) {
                return xsign;
            }
            mode = 1;
        }

        if (mode == 1) {
            String xsign = xsign1(xHeader);
            if (!HFStringUtils.isNullOrEmpty(xsign)) {
                return xsign;
            }
            mode = 3;
        }

//        if (mode == 2) {
//            String xsign = xsign2(xHeader);
//            if (!HFStringUtils.isNullOrEmpty(xsign)) {
//                return xsign;
//            }
//            mode = 3;
//        }
        return "";
    }

    @Override
    public String h5sign(H5Header h5Header, String postData) {
        if (h5Header.isExpired()) {
            return null;
        }

        String plain = h5Header.getToken() + "&" + String.valueOf(h5Header.getLongTimestamp()) + "&" + h5Header.getAppKey() + "&" + postData;
        return CryptoUtils.MD5(plain);
    }
}
