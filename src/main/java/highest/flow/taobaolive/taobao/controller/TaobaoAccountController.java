package highest.flow.taobaolive.taobao.controller;

import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.security.service.CryptoService;
import highest.flow.taobaolive.taobao.entity.TaobaoAccount;
import highest.flow.taobaolive.taobao.service.TaobaoAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/taobao")
public class TaobaoAccountController {

    @Autowired
    private CryptoService cryptoService;

    @Autowired
    private TaobaoAccountService taobaoAccountService;

    @PostMapping("/register")
    public R register(@RequestParam(name = "data") String data,
                      @RequestParam(name = "sign") String sign) {
        try {
            boolean verify = cryptoService.verify(data, sign);
            if (!verify) {
                return R.error("参数验证失败");
            }

            String plain = cryptoService.decrypt(data);

            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> map = jsonParser.parseMap(plain);

            String accountId = (String) map.get("account_id");
            String nick = (String) map.get("nick");
            String sid = (String) map.get("sid");
            String utdid = (String) map.get("utdid");
            String devid = (String) map.get("devid");
            String autoLoginToken = (String) map.get("auto_login_token");
            String cookie = (String) map.get("cookie");
            int expires = (int) map.get("expires");

            TaobaoAccount tbAccount = taobaoAccountService.register(accountId, nick, sid, utdid, devid, autoLoginToken, cookie, expires);
            if (tbAccount != null) {
                return R.ok();
            }

            return R.error();

        } catch (Exception ex) {
            return R.error("注册淘宝账号失败");
        }
    }

}
