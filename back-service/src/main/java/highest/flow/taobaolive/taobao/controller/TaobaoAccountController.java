package highest.flow.taobaolive.taobao.controller;

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
import org.apache.http.cookie.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

            newAccount.setUmtidToken(r.get("umtid"));

            r = taobaoApiService.getH5Token(newAccount);
            if (r.getCode() != ErrorCodes.SUCCESS) {
                return r;
            }

            r = taobaoApiService.getNewDeviceId(newAccount);

            if (r.getCode() != ErrorCodes.SUCCESS) {
                return r;
            }

            newAccount.setDevid(r.get("devid"));

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
                return R.error(INVALID_QRCODE_TOKEN, "请求Token无效");
            }

            // TODO

        } catch (Exception ex) {
            ex.printStackTrace();
            return R.error("验证二维码失败");
        }
    }

}
