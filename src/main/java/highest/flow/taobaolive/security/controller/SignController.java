package highest.flow.taobaolive.security.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.common.utils.HFStringUtils;
import highest.flow.taobaolive.taobao.entity.XHeader;
import highest.flow.taobaolive.security.service.CryptoService;
import highest.flow.taobaolive.taobao.service.XSignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SignController {

    @Autowired
    private CryptoService cryptoService;

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

            String xsign = xHeader.getXsign();
            if (!HFStringUtils.isNullOrEmpty(xsign)) {
                return R.ok("成功").put("xsign", xsign);
            }

            return R.error("xsign验证失败");

        } catch (Exception ex) {
            return R.error("参数验证失败");
        }
    }
}
