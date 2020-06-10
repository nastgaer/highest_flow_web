package highest.flow.taobaolive.security.controller;

import highest.flow.taobaolive.app.defines.HFUserLevel;
import highest.flow.taobaolive.app.entity.HFUser;
import highest.flow.taobaolive.app.entity.HFUserToken;
import highest.flow.taobaolive.app.service.HFUserService;
import highest.flow.taobaolive.app.service.ShiroService;
import highest.flow.taobaolive.common.defines.ErrorCodes;
import highest.flow.taobaolive.common.defines.ServiceType;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.security.service.CryptoService;
import highest.flow.taobaolive.security.service.LicenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/license")
public class LicenseController {

    @Autowired
    private CryptoService cryptoService;

    @Autowired
    private ShiroService shiroService;

    @Autowired
    private HFUserService hfUserService;

    @Autowired
    private LicenseService licenseService;

    @PostMapping("/generate")
    public R generateCode(@RequestParam(name = "data") String data,
                          @RequestParam(name = "sign") String sign,
                          @RequestHeader("token") String token) {
        try {
            boolean verify = cryptoService.verify(data, sign);
            if (!verify) {
                return R.error("参数验证失败");
            }

            String plain = cryptoService.decrypt(data);

            HFUserToken hfUserToken = shiroService.getUserTokenByToken(token);
            String username = hfUserToken.getUsername();
            HFUser hfUser = hfUserService.getUserByUsername(username);

            if (hfUser.getLevel() != HFUserLevel.Adminitrator.getLevel()) {
                return R.error(ErrorCodes.UNAUTHORIZED_USER, "没有权限");
            }

            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> map = jsonParser.parseMap(plain);

            int hours = (int) map.get("hours");
            ServiceType serviceType = ServiceType.fromInt((int) map.get("service_type"));

            String licenseCode = licenseService.generateCode(serviceType, hours);

            return R.ok().put("code", licenseCode);

        } catch (Exception ex) {
            return R.error("生成卡密失败");
        }
    }

    @PostMapping("/accept")
    public R acceptCode(@RequestParam(name = "data") String data,
                        @RequestParam(name = "sign") String sign) {
        try {
            boolean verify = cryptoService.verify(data, sign);
            if (!verify) {
                return R.error("参数验证失败");
            }

            String plain = cryptoService.decrypt(data);

            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> map = jsonParser.parseMap(plain);

            String code = (String) map.get("code");
            String machine = (String) map.get("machine_code");

            return licenseService.acceptCode(code, machine);

        } catch (Exception ex) {
            return R.error("验证卡密失败");
        }
    }

    @PostMapping("/bind")
    public R bindAccount(@RequestParam(name = "data") String data,
                         @RequestParam(name = "sign") String sign) {
        try {
            boolean verify = cryptoService.verify(data, sign);
            if (!verify) {
                return R.error("参数验证失败");
            }

            String plain = cryptoService.decrypt(data);

            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> map = jsonParser.parseMap(plain);

            String code = (String) map.get("code");
            String username = (String) map.get("username");
            String accountId = (String) map.get("account_id");
            String accountNick = (String) map.get("account_nick");

            return licenseService.bindAccount(code, username, accountId, accountNick);

        } catch (Exception ex) {
            return R.error("绑定卡密失败");
        }
    }
}
