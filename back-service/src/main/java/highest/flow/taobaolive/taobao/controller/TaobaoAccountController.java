package highest.flow.taobaolive.taobao.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import highest.flow.taobaolive.common.defines.ErrorCodes;
import highest.flow.taobaolive.common.http.CookieHelper;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.open.sys.PageParam;
import highest.flow.taobaolive.security.service.CryptoService;
import highest.flow.taobaolive.sys.entity.SysMember;
import highest.flow.taobaolive.taobao.entity.QRCode;
import highest.flow.taobaolive.taobao.entity.TaobaoAccount;
import highest.flow.taobaolive.taobao.service.TaobaoAccountService;
import highest.flow.taobaolive.taobao.service.TaobaoApiService;
import highest.flow.taobaolive.taobao.utils.DeviceUtils;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
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

    @Autowired
    private TaobaoApiService taobaoApiService;

    private Map<String, TaobaoAccount> waitingAccounts = new HashMap<>();

    @PostMapping("/list")
    public R list(@RequestBody PageParam pageParam) {
        try {
            List<TaobaoAccount> taobaoAccounts = this.taobaoAccountService.list();

            return R.ok().put("users", taobaoAccounts).put("total_count", taobaoAccounts.size());

        } catch (Exception ex) {
            ex.printStackTrace();
            return R.error("获取用户列表失败");
        }
    }

    @PostMapping("/qrcode")
    public R qrcode() {
        try {
            R r = taobaoApiService.getLoginQRCodeURL();
            if (r.getCode() != ErrorCodes.SUCCESS) {
                return r;
            }

            QRCode qrCode = (QRCode) r.get("qrCode");

            String accessToken = qrCode.getAccessToken();
            String url = qrCode.getNavigateUrl();

            TaobaoAccount newAccount = new TaobaoAccount();

            newAccount.setUtdid(DeviceUtils.generateUtdid());

            r = taobaoApiService.getUmtidToken();
            if (r.getCode() != ErrorCodes.SUCCESS) {
                return r;
            }

            newAccount.setUmidToken((String) r.get("umtid"));

            r = taobaoApiService.getH5Token(newAccount);
            if (r.getCode() != ErrorCodes.SUCCESS) {
                return r;
            }

            r = taobaoApiService.getNewDeviceId(newAccount);

            if (r.getCode() != ErrorCodes.SUCCESS) {
                return r;
            }

            newAccount.setDevid((String) r.get("devid"));

            waitingAccounts.put(accessToken, newAccount);

            return R.ok()
                .put("access_token", accessToken)
                .put("navigate_url", url);

        } catch (Exception ex) {
            ex.printStackTrace();
            return R.error("获取登录二维码失败");
        }
    }

    @PostMapping("/verify_qrcode")
    public R verifyQRCode(@RequestParam(name = "access_token") String accessToken) {
        try {
            TaobaoAccount account = waitingAccounts.get(accessToken);
            if (account == null) {
                return R.error(ErrorCodes.INVALID_QRCODE_TOKEN, "请求Token无效");
            }

            // TODO
            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
            return R.error("验证二维码失败");
        }
    }

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
                    @RequestParam(name = "created") @DateTimeFormat(pattern = "yyyy-MM-ss HH:mm:ss") Date created,
                    @RequestParam(name = "updated") @DateTimeFormat(pattern = "yyyy-MM-ss HH:mm:ss") Date updated) {

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

            TaobaoAccount taobaoAccount = taobaoAccountService.register(nick, nick, userId,
                    sid, utdid, devid, autoLoginToken, umidToken, cookies, expires, state, created, updated);
            if (taobaoAccount == null) {
                return R.error("保存数据库失败");
            }

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

}
