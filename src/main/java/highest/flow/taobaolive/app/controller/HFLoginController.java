package highest.flow.taobaolive.app.controller;

import highest.flow.taobaolive.app.entity.HFUser;
import highest.flow.taobaolive.app.service.HFUserService;
import highest.flow.taobaolive.app.service.HFUserTokenService;
import highest.flow.taobaolive.common.defines.ErrorCodes;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.security.service.CryptoService;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/sys")
public class HFLoginController {

    @Autowired
    private CryptoService cryptoService;

    @Autowired
    HFUserService hfUserService;

    @Autowired
    HFUserTokenService hfUserTokenService;

    @PostMapping("/register")
    public R registerUser(@RequestParam(name = "data") String data,
                          @RequestParam(name = "sign") String sign) {

        try {
            boolean verify = cryptoService.verify(data, sign);
            if (!verify) {
                return R.error("参数验证失败");
            }

            String plain = cryptoService.decrypt(data);

//            // Json to Object
//            ObjectMapper objectMapper = new ObjectMapper();
//            HFUser hfUser = objectMapper.readValue(plain, HFUser.class);

            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> map = jsonParser.parseMap(plain);

            String username = (String)map.get("username");
            String password = (String)map.get("password");
            String machineCode = (String)map.get("machineCode");
            String mobile = (String)map.get("mobile");
            String weixin = (String)map.get("weixin");
            int level = 0;
            int serviceType = (int)map.get("serviceType");

            HFUser hfUser = hfUserService.getById(username);
            if (hfUser != null) {
                return R.error(ErrorCodes.ALREADY_REGISTERED_USER, "已经注册好的账号");
            }

            hfUser = hfUserService.register(username, password, machineCode, mobile, weixin, level, serviceType);

            return R.ok();

        } catch (Exception ex) {
            return R.error("注册用户失败");
        }
    }

    @PostMapping("/login")
    public R login(@RequestParam(name = "data") String data,
                   @RequestParam(name = "sign") String sign) {
        try {
            boolean verify = cryptoService.verify(data, sign);
            if (!verify) {
                return R.error("参数验证失败");
            }

            String plain = cryptoService.decrypt(data);

            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> map = jsonParser.parseMap(plain);

            String username = (String)map.get("username");
            String password = (String)map.get("password");
            String machineCode = (String)map.get("machineCode");

            HFUser hfUser = hfUserService.getById(username);
            if (hfUser == null) {
                return R.error(ErrorCodes.NOT_FOUND_USER, "找不到用户");
            }

            if (!hfUser.getPassword().equals(new Sha256Hash(password, hfUser.getSalt()).toHex())) {
                return R.error(ErrorCodes.UNAUTHORIZED_USER, "账号或密码不正确");
            }
            if (!hfUser.getMachineCode().equals(machineCode)) {
                return R.error(ErrorCodes.UNAUTHORIZED_MACHINE, "机器码不正确");
            }

            R r = hfUserTokenService.createToken(username);

            return r;

        } catch (Exception ex) {
            return R.error("登录用户失败");
        }
    }

    @PostMapping("/logout")
    public R logout(@RequestParam(name = "data") String data,
                    @RequestParam(name = "sign") String sign) {
        try {
            boolean verify = cryptoService.verify(data, sign);
            if (!verify) {
                return R.error("参数验证失败");
            }

            String plain = cryptoService.decrypt(data);

            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> map = jsonParser.parseMap(plain);

            String username = (String)map.get("username");

            HFUser hfUser = hfUserService.getById(username);
            if (hfUser == null) {
                return R.error(ErrorCodes.NOT_FOUND_USER, "找不到用户");
            }

            R r = hfUserTokenService.createToken(username);

            return R.ok();

        } catch (Exception ex) {
            return R.error("注销用户失败");
        }
    }
}
