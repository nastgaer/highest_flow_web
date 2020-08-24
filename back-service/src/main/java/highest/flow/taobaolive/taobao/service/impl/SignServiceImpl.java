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

    @Override
    public String xsign(XHeader xHeader) {
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
    public String h5sign(H5Header h5Header, String postData) {
        if (h5Header.isExpired()) {
            return null;
        }

        String plain = h5Header.getToken() + "&" + String.valueOf(h5Header.getLongTimestamp()) + "&" + h5Header.getAppKey() + "&" + postData;
        return CryptoUtils.MD5(plain);
    }
}
