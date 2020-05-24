package highest.flow.taobaolive.security.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.common.utils.HFStringUtils;
import highest.flow.taobaolive.taobao.entity.XHeader;
import highest.flow.taobaolive.security.service.CryptoService;
import highest.flow.taobaolive.taobao.service.XSignService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SignController {

    @Autowired
    private CryptoService cryptoService;

    @Autowired
    private XSignService xSignService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @PostMapping("/xsign")
    public R xsign(@RequestParam(name = "data") String data, @RequestParam(name = "sign") String sign) {

        try {
            //logger.info(data);
            //logger.info(sign);

            boolean verify = cryptoService.verify(data, sign);
            if (!verify) {
                return R.error("参数验证失败");
            }

            String plain = cryptoService.decrypt(data);

            ObjectMapper objectMapper = new ObjectMapper();
            XHeader xHeader = objectMapper.readValue(plain, XHeader.class);

            String xsign = xSignService.sign1(xHeader);
            if (!HFStringUtils.isNullOrEmpty(xsign)) {
                return R.ok("成功").put("xsign", xsign).put("encoded", true);
            }
            xsign = xSignService.sign2(xHeader);
            if (!HFStringUtils.isNullOrEmpty(xsign)) {
                return R.ok("成功").put("xsign", xsign).put("encoded", false);
            }

            return R.error("xsign验证失败");

        } catch (Exception ex) {
            ex.printStackTrace();
            return R.error("参数验证失败");
        }
    }
}
