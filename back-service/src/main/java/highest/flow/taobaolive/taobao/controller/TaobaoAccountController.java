package highest.flow.taobaolive.taobao.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import highest.flow.taobaolive.common.annotation.SysLog;
import highest.flow.taobaolive.common.defines.ErrorCodes;
import highest.flow.taobaolive.common.http.CookieHelper;
import highest.flow.taobaolive.common.utils.*;
import highest.flow.taobaolive.api.param.PageParam;
import highest.flow.taobaolive.job.entity.ScheduleJobEntity;
import highest.flow.taobaolive.job.service.ScheduleJobService;
import highest.flow.taobaolive.job.utils.ScheduleUtils;
import highest.flow.taobaolive.security.service.CryptoService;
import highest.flow.taobaolive.sys.controller.AbstractController;
import highest.flow.taobaolive.sys.defines.MemberLevel;
import highest.flow.taobaolive.sys.entity.SysMember;
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
@RequestMapping("/v1.0/tbacc")
public class TaobaoAccountController extends AbstractController {

    @Autowired
    private TaobaoAccountService taobaoAccountService;

    @Autowired
    private TaobaoAccountLogService taobaoAccountLogService;

    @Autowired
    private TaobaoApiService taobaoApiService;

    @Autowired
    private CryptoService cryptoService;

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private ScheduleJobService schedulerJobService;

    private SelfExpiringMap<String, TaobaoAccountEntity> waitingAccounts = new SelfExpiringHashMap<>(30 * 60 * 1000);
    private SelfExpiringMap<String, QRCode> waitingQRCodes = new SelfExpiringHashMap<>(30 * 60 * 1000);

    @SysLog("??????????????????")
    @PostMapping("/list")
    public R list(@RequestBody PageParam pageParam) {
        try {
            SysMember sysMember = this.getUser();
            PageUtils pageUtils = this.taobaoAccountService.queryPage(sysMember, pageParam);

            int normalCount = taobaoAccountService.getNormalCount(sysMember, pageParam);
            int expiredCount = taobaoAccountService.getExpiredCount(sysMember, pageParam);

            return R.ok()
                    .put("users", pageUtils.getList())
                    .put("total_count", pageUtils.getTotalCount())
                    .put("normal_count", normalCount)
                    .put("expired_count", expiredCount);

        } catch (Exception ex) {
            ex.printStackTrace();
            return R.error("????????????????????????");
        }
    }

    @SysLog("???????????????")
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
                return R.error(ErrorCodes.INVALID_UMTID, "??????UMTID_TOKEN??????????????????????????????");
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
            return R.error("???????????????????????????");
        }
    }

    @SysLog("?????????????????????")
    @PostMapping("/verify_qrcode")
    public R verifyQRCode(@RequestBody Map<String, Object> params) {
        try {
            String accessToken = String.valueOf(params.get("access_token"));

            if (!waitingAccounts.containsKey(accessToken) || !waitingQRCodes.containsKey(accessToken)) {
                return R.error(ErrorCodes.INVALID_QRCODE_TOKEN, "??????Token??????");
            }

            TaobaoAccountEntity taobaoAccountEntity = waitingAccounts.get(accessToken);
            QRCode qrCode = waitingQRCodes.get(accessToken);
            if (qrCode == null) {
                return R.error(ErrorCodes.INVALID_QRCODE_TOKEN, "??????Token??????");
            }

            R r = taobaoApiService.checkLoginByQRCode(taobaoAccountEntity, qrCode);
            if (r.getCode() != ErrorCodes.SUCCESS) {
                return r;
            }

            SysMember sysMember = getUser();

            taobaoAccountEntity.setMemberId(sysMember.getId());
            taobaoAccountEntity.setCreatedTime(new Date());

            // ?????????????????????????????????
            TaobaoAccountEntity taobaoAccountEntityOther = taobaoAccountService.getInfoByUid(taobaoAccountEntity.getUid());
            if (taobaoAccountEntityOther != null) {
                taobaoAccountEntity.setId(taobaoAccountEntityOther.getId());
                this.taobaoAccountService.updateById(taobaoAccountEntity);
            } else {
                this.taobaoAccountService.save(taobaoAccountEntity);
            }

            waitingAccounts.remove(accessToken);
            waitingQRCodes.remove(accessToken);

            return r;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error("?????????????????????");
    }

    @SysLog("??????????????????")
    @PostMapping(value = "/batch_delete")
    public R batchDelete(@RequestBody Map<String, Object> params) {
        try {
            List<String> nicks = (List<String>) params.get("user_ids");

            if (taobaoAccountService.remove(Wrappers.<TaobaoAccountEntity>lambdaQuery()
                    .in(TaobaoAccountEntity::getNick, nicks))) {
                return R.ok();
            }

            return R.error();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error("??????????????????????????????");
    }

    @SysLog("????????????")
    @PostMapping("/postpone")
    public R postpone(@RequestBody Map<String, Object> params) {
        try {
            String crond = (String) params.get("crond");

            ScheduleJobEntity scheduleJobEntity = this.schedulerJobService.getOne(Wrappers.<ScheduleJobEntity>lambdaQuery()
                    .eq(ScheduleJobEntity::getBeanName, "autoLoginTask"));
            if (scheduleJobEntity == null) {
                return R.error("???????????????JOB");
            }

            scheduleJobEntity.setCronExpression(crond);
            ScheduleUtils.updateScheduleJob(scheduler, scheduleJobEntity);

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error("????????????????????????");
    }

    @SysLog("????????????????????????")
    @PostMapping("/logs")
    public R logs(@RequestBody PageParam pageParam) {
        try {
            SysMember sysMember = this.getUser();
            PageUtils pageUtils = this.taobaoAccountLogService.queryPage(sysMember, pageParam);

            return R.ok().put("logs", pageUtils.getList()).put("total_count", pageUtils.getTotalCount());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error("??????????????????????????????");
    }

    @SysLog("????????????????????????")
    @PostMapping("/upload")
    public R upload(@RequestParam(name = "user_id") String userId,
                    @RequestParam(name = "nick") String nick,
                    @RequestParam(name = "sid") String sid,
                    @RequestParam(name = "utdid") String utdid,
                    @RequestParam(name = "devid") String devid,
                    @RequestParam(name = "auto_login_token") String autoLoginToken,
                    @RequestParam(name = "umid_token") String umidToken,
                    @RequestParam(name = "cookies[]") String[] cookieHeaders,
                    @RequestParam(name = "expires") long expires,
                    @RequestParam(name = "state") int state,
                    @RequestParam(name = "created") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date created,
                    @RequestParam(name = "updated") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date updated) {

        try {
            List<Cookie> cookies = new ArrayList<>();
            for (String cookieHeader : cookieHeaders) {
                Cookie cookie = CookieHelper.parseString(cookieHeader);
                if (cookie != null) {
                    cookies.add(cookie);
                }
            }

            TaobaoAccountEntity taobaoAccountEntity = taobaoAccountService.register(null, nick, userId,
                    sid, utdid, devid, autoLoginToken, umidToken, cookies, expires, state, created, updated);
            if (taobaoAccountEntity == null) {
                return R.error("?????????????????????");
            }

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

//    @PostMapping("/download2")
//    public R download2(@RequestBody Map<String, Object> param) {
//        try {
//            String encrypt = (String)param.get("encrypt");
//            String decypt = (String) param.get("decrypt");
//
//            String decrypted = cryptoService.decrypt(encrypt);
//            if (decypt.compareTo(decrypted) == 0) {
//                return R.ok().put("encrypt", cryptoService.encrypt(decrypted));
//            }
//
//            return R.error();
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return R.error();
//    }

    @PostMapping("/download")
    public R download(@RequestBody PageParam pageParam) {
        try {
            SysMember sysMember = this.getUser();
            PageUtils pageUtils = this.taobaoAccountService.queryPage(sysMember, pageParam);

            List<TaobaoAccountEntity> taobaoAccountEntities = pageUtils.getList();

            ObjectMapper objectMapper = new ObjectMapper();
            List<String> encrypted = new ArrayList<>();
            List<String> signs = new ArrayList<>();
            for (TaobaoAccountEntity taobaoAccountEntity : taobaoAccountEntities) {
                Map<String, Object> map = new HashMap<>();
                map.put("nick", taobaoAccountEntity.getNick());
                map.put("sid", taobaoAccountEntity.getSid());
                map.put("uid", taobaoAccountEntity.getUid());
                map.put("utdid", taobaoAccountEntity.getUtdid());
                map.put("devid", taobaoAccountEntity.getDevid());
                map.put("auto_login_token", taobaoAccountEntity.getAutoLoginToken());
                map.put("umid_token", taobaoAccountEntity.getUmidToken());
                map.put("expires", CommonUtils.formatDate(taobaoAccountEntity.getExpires()));
                map.put("state", taobaoAccountEntity.getState());
                map.put("cookie", taobaoAccountEntity.getCookie());

                String jsonText = objectMapper.writeValueAsString(map);
                String encrypt = cryptoService.encrypt(jsonText);
                String sign = cryptoService.sign(encrypt);
                encrypted.add(encrypt);
                signs.add(sign);
            }

            String signConcat = "";
            for (String text : signs) {
                signConcat += text;
            }

            String sign = cryptoService.sign(signConcat);

            return R.ok()
                    .put("users", encrypted)
                    .put("sign", sign)
                    .put("total_count", pageUtils.getTotalCount())
                    .put("total_page", pageUtils.getTotalPage())
                    .put("page_size", pageUtils.getPageSize())
                    .put("curr_page", pageUtils.getCurrPage());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }
}
