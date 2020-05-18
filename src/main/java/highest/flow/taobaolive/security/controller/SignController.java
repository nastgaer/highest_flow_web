package highest.flow.taobaolive.security.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import highest.flow.taobaolive.common.utils.HttpUtils;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.common.utils.StringUtils;
import highest.flow.taobaolive.security.entity.XHeader;
import highest.flow.taobaolive.security.service.CryptoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sun.reflect.annotation.ExceptionProxy;

import java.net.URLEncoder;

@RestController
public class SignController {

    @Autowired
    private CryptoService cryptoService;

    private int[] availablePorts = new int [] {
            59316, 58119, 58114, 58120
    };
    private int port2 = 59316;

    private String prepareXSign1(XHeader xHeader) {
        try {
            String plain = xHeader.getUtdid() + "&" + xHeader.getUid() + "&&" + xHeader.getAppkey() + "&" + xHeader.getAes() + "&" +
                    xHeader.getTimestamp() + "&" + xHeader.getUrl() + "&" + xHeader.getUrlVer() + "&" +
                    xHeader.getSid() + "&" + "600000@taobao_android_7.6.0" + "&" + xHeader.getDevid() + "&" +
                    "454.451236&1568.459875" + "&" + xHeader.getFeatures();

            String timeMD5 = cryptoService.MD5(xHeader.getTimestamp());

            String url = "http://39.100.74.215:2345/x-sign.php?" + plain;

            String respText = HttpUtils.doGet(url);

            if (respText == null) {
                return null;
            }

            respText = StringUtils.trim(respText, new char[] {
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
                    xHeader.getTimestamp() + "%26" + xHeader.getUrl() + "%26" + xHeader.getUrlVer() + "%26" +
                    xHeader.getSid() + "%26" + xHeader.getTtid() + "%26" + xHeader.getDevid() + "%26" +
                    xHeader.getLocation() + "%26" + xHeader.getFeatures();

            String timeMD5 = cryptoService.MD5(xHeader.getTimestamp());

            String url = "http://1.192.134.231:" + port + "/xdata?data=" + plain + "&apiKey=&t=&apiSign=" + URLEncoder.encode(timeMD5);;

            String respText = HttpUtils.doGet(url);

            if (respText == null) {
                return null;
            }

            respText = StringUtils.trim(respText, new char[] {
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

    @PostMapping("/xsign")
    public R xsign(@RequestParam(name = "data") String data, @RequestParam(name = "sign") String sign) {

        try {
            boolean verify = cryptoService.verify(data, sign);
            if (!verify) {
                return R.error("参数验证失败");
            }

            String plain = cryptoService.decrypt(data);

            ObjectMapper objectMapper = new ObjectMapper();
            XHeader xHeader = objectMapper.readValue(plain, XHeader.class);

            String xsign = prepareXSign1(xHeader);
            if (!StringUtils.isNullOrEmpty(xsign)) {
                return R.ok("成功").put("xsign", xsign);
            }

            xsign = prepareXSign2(xHeader, port2);
            if (!StringUtils.isNullOrEmpty(xsign)) {
                return R.ok("成功").put("xsign", xsign);
            }

            for (int port : availablePorts) {
                xsign = prepareXSign2(xHeader, port);
                if (!StringUtils.isNullOrEmpty(xsign)) {
                    port2 = port;
                    return R.ok("成功").put("xsign", xsign);
                }
            }
            return R.error("xsign验证失败");

        } catch (Exception ex) {
            return R.error("参数验证失败");
        }
    }
}
