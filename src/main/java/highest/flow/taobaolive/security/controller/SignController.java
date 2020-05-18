package highest.flow.taobaolive.security.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import highest.flow.taobaolive.common.utils.HttpUtils;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.security.entity.XHeader;
import highest.flow.taobaolive.security.service.CryptoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;

@RestController
public class SignController {

    @Autowired
    private CryptoService cryptoService;

    private int port = 59316;

    @GetMapping("/greeting")
    public String greeting(@RequestParam(value="name", defaultValue = "World") String name) {
        return "Hello " + name;
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

            plain = xHeader.getUtdid() + "%26" + xHeader.getUid() + "%26%26" + xHeader.getAppkey() + "%26" + xHeader.getAes() + "%26" +
                    xHeader.getTimestamp() + "%26" + xHeader.getUrl() + "%26" + xHeader.getUrlVer() + "%26" +
                    xHeader.getSid() + "%26" + xHeader.getTtid() + "%26" + xHeader.getDevid() + "%26" +
                    xHeader.getLocation() + "%26" + xHeader.getFeatures();


            String timeMD5 = cryptoService.MD5(xHeader.getTimestamp());

            String url = "http://1.192.134.231:" + port + "/xdata?data=" + plain + "&apiKey=&t=&apiSign=" + URLEncoder.encode(timeMD5);;

            String respText = HttpUtils.doGet(url);

            return R.ok(respText);

        } catch (Exception ex) {
            return R.error("参数验证失败");
        }
    }
}
