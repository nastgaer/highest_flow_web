package highest.flow.taobaolive.taobao.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import highest.flow.taobaolive.common.defines.ErrorCodes;
import highest.flow.taobaolive.common.http.CookieHelper;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.common.utils.SelfExpiringHashMap;
import highest.flow.taobaolive.common.utils.SelfExpiringMap;
import highest.flow.taobaolive.sys.entity.PageEntity;
import highest.flow.taobaolive.sys.controller.AbstractController;
import highest.flow.taobaolive.taobao.entity.QRCode;
import highest.flow.taobaolive.taobao.entity.TaobaoAccountEntity;
import highest.flow.taobaolive.taobao.service.TaobaoAccountService;
import highest.flow.taobaolive.taobao.service.TaobaoApiService;
import highest.flow.taobaolive.taobao.utils.DeviceUtils;
import org.apache.http.cookie.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/tbacc")
public class TaobaoAccountController extends AbstractController {

    @Autowired
    private TaobaoAccountService taobaoAccountService;

    @Autowired
    private TaobaoApiService taobaoApiService;

    private SelfExpiringMap<String, TaobaoAccountEntity> waitingAccounts = new SelfExpiringHashMap<>(30 * 60 * 1000);
    private SelfExpiringMap<String, QRCode> waitingQRCodes = new SelfExpiringHashMap<>(30 * 60 * 1000);

    @PostMapping("/list")
    public R list(@RequestBody PageEntity pageEntity) {
        try {
            int pageNo = pageEntity.getPageNo();
            int pageSize = pageEntity.getPageSize();
            IPage<TaobaoAccountEntity> page = this.taobaoAccountService.page(new Page<>((pageNo - 1) * pageSize, pageSize));
            List<TaobaoAccountEntity> taobaoAccountEntities = this.taobaoAccountService.list();

            return R.ok().put("users", taobaoAccountEntities).put("total_count", taobaoAccountService.size());

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
            String imageUrl = qrCode.getImageUrl();

            TaobaoAccountEntity newAccount = new TaobaoAccountEntity();

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
            waitingQRCodes.put(accessToken, qrCode);

            return R.ok()
                .put("access_token", accessToken)
                .put("navigate_url", imageUrl);

        } catch (Exception ex) {
            ex.printStackTrace();
            return R.error("获取登录二维码失败");
        }
    }

    @PostMapping("/verify_qrcode")
    public R verifyQRCode(@RequestParam(name = "access_token") String accessToken) {
        try {
            if (!waitingAccounts.containsKey(accessToken) || !waitingQRCodes.containsKey(accessToken)) {
                return R.error(ErrorCodes.INVALID_QRCODE_TOKEN, "请求Token无效");
            }

            TaobaoAccountEntity taobaoAccountEntity = waitingAccounts.get(accessToken);
            QRCode qrCode = waitingQRCodes.get(accessToken);
            if (qrCode == null) {
                return R.error(ErrorCodes.INVALID_QRCODE_TOKEN, "请求Token无效");
            }

            return taobaoApiService.checkLoginByQRCode(taobaoAccountEntity, qrCode);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error("验证二维码失败");
    }

    @PostMapping("/delete")
    public R delete(@RequestParam(name = "user_ids") String param) {
        try {
            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            List userIds = jsonParser.parseList(param);

            List<String> ids = new ArrayList<>();
            for (Object obj : userIds) {
                ids.add(String.valueOf(obj));
            }
            if (taobaoAccountService.removeByIds(ids)) {
                return R.ok();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error("批量删除扫码信息失败");
    }

    @PostMapping("/postpone")
    public R postpone(@RequestParam(name = "crond") String crond) {
        try {
            // TODO
            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error("设置延期公式失败");
    }

    @PostMapping("/logs")
    public R logs(@RequestBody PageEntity pageEntity) {
        try {
            // TODO
            return R.ok()
                    .put("logs", new ArrayList<>());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error("设置延期公式失败");
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

            TaobaoAccountEntity taobaoAccountEntity = taobaoAccountService.register(nick, nick, userId,
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
