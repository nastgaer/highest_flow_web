package highest.flow.taobaolive.taobao.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import highest.flow.taobaolive.common.defines.ErrorCodes;
import highest.flow.taobaolive.common.http.CookieHelper;
import highest.flow.taobaolive.common.utils.*;
import highest.flow.taobaolive.api.param.PageParam;
import highest.flow.taobaolive.job.entity.ScheduleJobEntity;
import highest.flow.taobaolive.job.service.ScheduleJobService;
import highest.flow.taobaolive.job.utils.ScheduleUtils;
import highest.flow.taobaolive.sys.controller.AbstractController;
import highest.flow.taobaolive.taobao.defines.TaobaoAccountState;
import highest.flow.taobaolive.taobao.entity.QRCode;
import highest.flow.taobaolive.taobao.entity.TaobaoAccountEntity;
import highest.flow.taobaolive.taobao.entity.TaobaoAccountLogEntity;
import highest.flow.taobaolive.taobao.service.TaobaoAccountLogService;
import highest.flow.taobaolive.taobao.service.TaobaoAccountService;
import highest.flow.taobaolive.taobao.service.TaobaoApiService;
import highest.flow.taobaolive.taobao.utils.DeviceUtils;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.sql.Wrapper;
import java.util.*;

@RestController
@RequestMapping("/tbacc")
public class TaobaoAccountController extends AbstractController {

    @Autowired
    private TaobaoAccountService taobaoAccountService;

    @Autowired
    private TaobaoAccountLogService taobaoAccountLogService;

    @Autowired
    private TaobaoApiService taobaoApiService;

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private ScheduleJobService schedulerJobService;

    private SelfExpiringMap<String, TaobaoAccountEntity> waitingAccounts = new SelfExpiringHashMap<>(30 * 60 * 1000);
    private SelfExpiringMap<String, QRCode> waitingQRCodes = new SelfExpiringHashMap<>(30 * 60 * 1000);

    @PostMapping("/list")
    public R list(@RequestBody PageParam pageParam) {
        try {
            int pageNo = pageParam.getPageNo();
            int pageSize = pageParam.getPageSize();
            String keyword = pageParam.getKeyword();

            IPage<TaobaoAccountEntity> page = HFStringUtils.isNullOrEmpty(keyword) ?
                    this.taobaoAccountService.page(new Page<>((pageNo - 1) * pageSize, pageSize)) :
                    this.taobaoAccountService.page(new Page<>((pageNo - 1) * pageSize, pageSize),
                            Wrappers.<TaobaoAccountEntity>lambdaQuery().like(TaobaoAccountEntity::getNick, keyword));
            List<TaobaoAccountEntity> taobaoAccountEntities = page.getRecords();
            return R.ok().put("users", taobaoAccountEntities).put("total_count", taobaoAccountService.count());

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

            QRCode qrCode = (QRCode) r.get("qrcode");

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

            newAccount.setDevid((String) r.get("device_id"));

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
    public R verifyQRCode(@RequestBody Map<String, Object> params) {
        try {
            String accessToken = String.valueOf(params.get("access_token"));

            if (!waitingAccounts.containsKey(accessToken) || !waitingQRCodes.containsKey(accessToken)) {
                return R.error(ErrorCodes.INVALID_QRCODE_TOKEN, "请求Token无效");
            }

            TaobaoAccountEntity taobaoAccountEntity = waitingAccounts.get(accessToken);
            QRCode qrCode = waitingQRCodes.get(accessToken);
            if (qrCode == null) {
                return R.error(ErrorCodes.INVALID_QRCODE_TOKEN, "请求Token无效");
            }

            R r = taobaoApiService.checkLoginByQRCode(taobaoAccountEntity, qrCode);
            if (r.getCode() != ErrorCodes.SUCCESS) {
                return r;
            }

            taobaoAccountEntity.setCreatedTime(new Date());

            taobaoAccountService.save(taobaoAccountEntity);

            waitingAccounts.remove(accessToken);
            waitingQRCodes.remove(accessToken);

            return r;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error("验证二维码失败");
    }

    @PostMapping(value = "/batch_delete")
    public R batchDelete(@RequestBody Map<String, Object> params) {
        try {
            List<String> nicks = (List<String>) params.get("nicks");

            if (taobaoAccountService.remove(Wrappers.<TaobaoAccountEntity>lambdaQuery()
                    .in(TaobaoAccountEntity::getNick, nicks))) {
                return R.ok();
            }

            return R.error();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error("批量删除扫码信息失败");
    }

    @PostMapping("/postpone")
    public R postpone(@RequestBody Map<String, Object> params) {
        try {
            String crond = (String) params.get("crond");

            ScheduleJobEntity scheduleJobEntity = this.schedulerJobService.getOne(Wrappers.<ScheduleJobEntity>lambdaQuery()
                    .eq(ScheduleJobEntity::getBeanName, "autoLoginTask"));
            if (scheduleJobEntity == null) {
                return R.error("找不到延期JOB");
            }

            scheduleJobEntity.setCronExpression(crond);
            ScheduleUtils.updateScheduleJob(scheduler, scheduleJobEntity);

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error("设置延期公式失败");
    }

    @PostMapping("/logs")
    public R logs(@RequestBody PageParam pageParam) {
        try {
            int pageNo = pageParam.getPageNo();
            int pageSize = pageParam.getPageSize();
            String keyword = pageParam.getKeyword();

            IPage<TaobaoAccountLogEntity> page = HFStringUtils.isNullOrEmpty(keyword) ?
                    this.taobaoAccountLogService.page(new Page<>((pageNo - 1) * pageSize, pageSize)) :
                    this.taobaoAccountLogService.page(new Page<>((pageNo - 1) * pageSize, pageSize),
                            Wrappers.<TaobaoAccountLogEntity>lambdaQuery().like(TaobaoAccountLogEntity::getNick, keyword));
            List<TaobaoAccountLogEntity> logs = page.getRecords();
            return R.ok().put("logs", logs).put("total_count", taobaoAccountLogService.count());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error("获取账号操作记录失败");
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
