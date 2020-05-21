package highest.flow.taobaolive.taobao.service.impl;

import highest.flow.taobaolive.common.utils.HFStringUtils;
import highest.flow.taobaolive.common.utils.HttpUtils;
import highest.flow.taobaolive.security.service.CryptoService;
import highest.flow.taobaolive.taobao.entity.XHeader;
import highest.flow.taobaolive.taobao.service.XSignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;

@Service("xSignService")
public class XSignServiceImpl implements XSignService {

    @Autowired
    private CryptoService cryptoService;

    private int[] availablePorts = new int [] {
            59316, 58119, 58114, 58120
    };
    private int port2 = 59316;

    private String prepareXSign1(XHeader xHeader) {
        try {
            String plain = xHeader.getUtdid() + "&" + xHeader.getUid() + "&&" + xHeader.getAppkey() + "&" + xHeader.getAes() + "&" +
                    xHeader.getShortTimestamp() + "&" + xHeader.getUrl() + "&" + xHeader.getUrlVer() + "&" +
                    xHeader.getSid() + "&" + "600000@taobao_android_7.6.0" + "&" + xHeader.getDevid() + "&" +
                    "454.451236&1568.459875" + "&" + xHeader.getFeatures();

            String timeMD5 = cryptoService.MD5(String.valueOf(xHeader.getLongTimestamp()));

            String url = "http://39.100.74.215:2345/x-sign.php?" + plain;

            String respText = HttpUtils.doGet(url);

            if (respText == null) {
                return null;
            }

            respText = HFStringUtils.trim(respText, new char[] {
                    '\"', '\r', '\n', ' '
            });

            if (respText.startsWith("ab2")) {
                return respText;
            }
            return null;

        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
        return null;
    }

    private String prepareXSign2(XHeader xHeader, int port) {
        try {
            String plain = xHeader.getUtdid() + "%26" + xHeader.getUid() + "%26%26" + xHeader.getAppkey() + "%26" + xHeader.getAes() + "%26" +
                    xHeader.getShortTimestamp() + "%26" + xHeader.getUrl() + "%26" + xHeader.getUrlVer() + "%26" +
                    xHeader.getSid() + "%26" + xHeader.getTtid() + "%26" + xHeader.getDevid() + "%26" +
                    xHeader.getLocation() + "%26" + xHeader.getFeatures();

            String timeMD5 = cryptoService.MD5(String.valueOf(xHeader.getLongTimestamp()));

            String url = "http://1.192.134.231:" + port + "/xdata?data=" + plain + "&apiKey=&t=&apiSign=" + URLEncoder.encode(timeMD5);;

            String respText = HttpUtils.doGet(url);

            if (respText == null) {
                return null;
            }

            respText = HFStringUtils.trim(respText, new char[] {
                    '\"', '\r', '\n', ' '
            });

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
        String xsign = prepareXSign1(xHeader);
        if (!HFStringUtils.isNullOrEmpty(xsign)) {
            return xsign;
        }

        xsign = prepareXSign2(xHeader, port2);
        if (!HFStringUtils.isNullOrEmpty(xsign)) {
            return xsign;
        }

        for (int port : availablePorts) {
            xsign = prepareXSign2(xHeader, port);
            if (!HFStringUtils.isNullOrEmpty(xsign)) {
                port2 = port;
                return xsign;
            }
        }
        return null;
    }
}
