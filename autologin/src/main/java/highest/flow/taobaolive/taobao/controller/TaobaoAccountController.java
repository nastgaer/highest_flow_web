package highest.flow.taobaolive.taobao.controller;

import highest.flow.taobaolive.common.http.CookieHelper;
import highest.flow.taobaolive.common.utils.*;
import highest.flow.taobaolive.taobao.entity.TaobaoAccountEntity;
import highest.flow.taobaolive.taobao.service.TaobaoAccountService;
import org.apache.http.cookie.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/tbacc")
public class TaobaoAccountController {

    @Autowired
    private TaobaoAccountService taobaoAccountService;

    @PostMapping("/upload")
    public R upload(@RequestParam(name = "user_id") String userId,
                    @RequestParam(name = "nick") String nick,
                    @RequestParam(name = "sid") String sid,
                    @RequestParam(name = "utdid") String utdid,
                    @RequestParam(name = "devid") String devid,
                    @RequestParam(name = "auto_login_token") String autoLoginToken,
                    @RequestParam(name = "umid_token") String umidToken,
                    @RequestParam(name = "cookies[]") String [] cookieHeaders,
                    @RequestParam(name = "expires") long expires,
                    @RequestParam(name = "state") int state,
                    @RequestParam(name = "created") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date created,
                    @RequestParam(name = "updated") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date updated) {

        try {
            JsonParser jsonParser = JsonParserFactory.getJsonParser();

            String url = "https://api.m.taobao.com/gw/mtop.taobao.havana.mlogin.qrcodelogin/1.0/";
            List<Cookie> cookies = new ArrayList<>();
            for (String cookieHeader : cookieHeaders) {
                Cookie cookie = CookieHelper.parseString(cookieHeader);
                if (cookie != null) {
                    cookies.add(cookie);
                }
            }

            TaobaoAccountEntity taobaoAccountEntity = taobaoAccountService.register(nick, userId,
                    sid, utdid, devid, autoLoginToken, umidToken, cookies, expires, state, created, updated);
            if (taobaoAccountEntity == null) {
                return R.error("保存数据库失败");
            }

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

}
