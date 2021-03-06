package highest.flow.taobaolive.external.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import highest.flow.taobaolive.api.param.AddRankingTaskParam;
import highest.flow.taobaolive.api.param.AddRankingTaskParam2;
import highest.flow.taobaolive.api.param.PageParam;
import highest.flow.taobaolive.common.annotation.SysLog;
import highest.flow.taobaolive.common.config.Config;
import highest.flow.taobaolive.common.defines.ErrorCodes;
import highest.flow.taobaolive.common.utils.CommonUtils;
import highest.flow.taobaolive.common.utils.PageUtils;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.external.params.BaseParam;
import highest.flow.taobaolive.security.defines.LicenseCodeState;
import highest.flow.taobaolive.security.defines.LicenseCodeType;
import highest.flow.taobaolive.security.entity.LicenseCode;
import highest.flow.taobaolive.security.service.CryptoService;
import highest.flow.taobaolive.security.service.LicenseCodeService;
import highest.flow.taobaolive.sys.controller.AbstractController;
import highest.flow.taobaolive.sys.defines.MemberServiceType;
import highest.flow.taobaolive.sys.defines.MemberState;
import highest.flow.taobaolive.sys.entity.SysMember;
import highest.flow.taobaolive.sys.service.MemberService;
import highest.flow.taobaolive.sys.service.MemberTokenService;
import highest.flow.taobaolive.taobao.defines.RankingScore;
import highest.flow.taobaolive.taobao.entity.LiveRoomEntity;
import highest.flow.taobaolive.taobao.entity.RankingEntity;
import highest.flow.taobaolive.taobao.entity.TaobaoAccountEntity;
import highest.flow.taobaolive.taobao.service.RankingService;
import highest.flow.taobaolive.taobao.service.TaobaoAccountService;
import highest.flow.taobaolive.taobao.service.TaobaoApiService;
import org.aspectj.apache.bcel.generic.RET;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/external/ranking")
public class RankingClientController extends AbstractController {

    @Autowired
    private LicenseCodeService licenseCodeService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberTokenService memberTokenService;

    @Autowired
    private TaobaoAccountService taobaoAccountService;

    @Autowired
    private TaobaoApiService taobaoApiService;

    @Autowired
    private RankingService rankingService;

    @Autowired
    private CryptoService cryptoService;

    @PostMapping("/reg")
    public R reg(@RequestBody BaseParam param) {
        try {
            if (param.getApi().compareTo("register_code") != 0) {
                return R.error(ErrorCodes.NOT_FOUND_REMOTE_FUNC, "??????????????????");
            }

            String data = param.getData();
            String sign = param.getSign();

            boolean verify = cryptoService.verify(data, sign);
            if (!verify) {
                return R.error("??????????????????");
            }

            String plain = cryptoService.decrypt(data);

            R r = callApi(param.getVersion(),
                    param.getApi(),
                    plain);

            // LOGGING
            Map<String, Object> map = new HashMap<>();
            map.put("version", param.getVersion());
            map.put("method", param.getApi());
            map.put("params", plain);
            map.put("returns", r);
            ObjectMapper objectMapper = new ObjectMapper();
            String log = objectMapper.writeValueAsString(map);
            logger.info(log);

            if (r.getCode() != ErrorCodes.SUCCESS) {
                return r;
            }

            Map<String, Object> mapData = r.getData();

            r = R.ok();
            for (String key : mapData.keySet()) {
                r.put(key, mapData.get(key));
            }
            return r;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @PostMapping("/api")
    public R api(@RequestBody BaseParam param) {
        try {
            String data = param.getData();
            String sign = param.getSign();

            boolean verify = cryptoService.verify(data, sign);
            if (!verify) {
                return R.error("??????????????????");
            }

            String plain = cryptoService.decrypt(data);

            R r = callApi(param.getVersion(),
                    param.getApi(),
                    plain);

            // LOGGING
            Map<String, Object> map = new HashMap<>();
            map.put("version", param.getVersion());
            map.put("method", param.getApi());
            map.put("params", plain);
            map.put("returns", r);
            ObjectMapper objectMapper = new ObjectMapper();
            String log = objectMapper.writeValueAsString(map);
            logger.info(log);

            if (r.getCode() != ErrorCodes.SUCCESS) {
                return r;
            }

            Map<String, Object> mapData = r.getData();

            r = R.ok();
            for (String key : mapData.keySet()) {
                r.put(key, mapData.get(key));
            }
            return r;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    public R callApi(String version, String api, String plain) {
        try {
            SysMember sysMember = this.getUser();
            if (api.compareTo("register_code") != 0 && sysMember == null) {
                return R.error(ErrorCodes.NOT_FOUND_USER, "???????????????");
            }

            String methodName = version.replace(".", "_") + "_" + api;

            Method[] methods = this.getClass().getMethods();
            for (Method method : methods) {
                if (method.getName().compareTo(methodName) == 0) {
                    return (R) method.invoke(this, sysMember, plain);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error(ErrorCodes.NOT_FOUND_REMOTE_FUNC, "???????????????");
    }

    @SysLog("????????????")
    public R v1_1_register_code(SysMember member, String plain) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> map = (Map<String, Object>) objectMapper.readValue(plain, Map.class);

            String code = (String) map.get("code");
            String machineCode = (String) map.get("machineCode");

            LicenseCode licenseCode = licenseCodeService.getCodeDesc(code);
            if (licenseCode == null) {
                return R.error(ErrorCodes.NOT_FOUND_CODE, "???????????????");
            }

            if (licenseCode.getState() == LicenseCodeState.Deleted.getState()) {
                return R.error(ErrorCodes.INVALID_CODE, "???????????????");
            }

            SysMember sysMember = null;
            if (licenseCode.getState() == LicenseCodeState.Created.getState()) {
                // ?????????????????????????????????
                sysMember = this.memberService.createLicenseMember(LicenseCodeType.from(licenseCode.getCodeType()),
                        MemberServiceType.from(licenseCode.getServiceType()));

                licenseCode.setMachineCode(machineCode);
                licenseCode.setState(LicenseCodeState.Accepted.getState());
                licenseCode.setMemberId(sysMember.getId());
                Date serviceStartTime = new Date();
                Date serviceEndTime = CommonUtils.addHours(serviceStartTime, licenseCode.getHours());
                licenseCode.setServiceStartTime(serviceStartTime);
                licenseCode.setServiceEndTime(serviceEndTime);

                licenseCode.setAcceptedTime(new Date());
                this.licenseCodeService.updateById(licenseCode);

            } else {
                sysMember = this.memberService.getById(licenseCode.getMemberId());

            }

            if (licenseCode.getState() == LicenseCodeState.Deleted.getState()) {
                return R.error(ErrorCodes.INVALID_CODE, "??????????????????");
            }

            if (sysMember == null) {
                return R.error(ErrorCodes.NOT_FOUND_USER, "???????????????");
            }

            if (licenseCode.getMachineCode().compareTo(machineCode) != 0) {
                return R.error(ErrorCodes.UNAUTHORIZED_MACHINE, "?????????????????????");
            }

            if (sysMember.getState() != MemberState.Normal.getState()) {
                return R.error(ErrorCodes.UNALLOWED_USER, "???????????????????????????????????????");
            }

            // ??????
            R r = memberTokenService.createToken(sysMember.getId());
            String accessToken = (String) r.get("access_token");

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            return R.ok()
                    .put("access_token", accessToken)
                    .put("service_start_time", sdf.format(licenseCode.getServiceStartTime()))
                    .put("service_end_time", sdf.format(licenseCode.getServiceEndTime()));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @SysLog("????????????????????????")
    /**
     * ??????????????????
     * @param plain
     * @return
     */
    public R v1_1_tbacc(SysMember sysMember, String plain) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            PageParam pageParam = (PageParam) objectMapper.readValue(plain, PageParam.class);

            PageUtils pageUtils =
                    this.taobaoAccountService.queryPage(sysMember, pageParam);

            List<TaobaoAccountEntity> taobaoAccountEntities = pageUtils.getList();

            List<Map> list = new ArrayList<>();
            for (TaobaoAccountEntity taobaoAccountEntity : taobaoAccountEntities) {
                Map<String, Object> map = new HashMap<>();
                map.put("nick", taobaoAccountEntity.getNick());
                map.put("uid", taobaoAccountEntity.getUid());
                map.put("state", taobaoAccountEntity.getState());
                list.add(map);
            }

//            logger.info("?????????Cache????????????");
//
//            // Cache???????????????
//            Cache cache = cacheManager.getCache("getActiveAllByMember");
//            int key = sysMember.getId();
//            Cache.ValueWrapper valueWrapper = cache.get(key);
//            List<TaobaoAccountEntity> cachedAccounts = valueWrapper == null ? new ArrayList<>() : (List<TaobaoAccountEntity>)valueWrapper.get();
//
//            logger.info("???????????????????????????????????????");
//
//            for (TaobaoAccountEntity taobaoAccountEntity : taobaoAccountEntities) {
//                boolean found = false;
//                for (TaobaoAccountEntity cachedAccount : cachedAccounts) {
//                    if (cachedAccount.getUid().compareTo(taobaoAccountEntity.getUid()) == 0) {
//                        found = true;
//                        break;
//                    }
//                }
//                if (found) {
//                    continue;
//                }
//                cachedAccounts.add(taobaoAccountEntity);
//            }
//
//            cache.put(key, cachedAccounts);
//
//            logger.info("??????????????????");

            return R.ok()
                    .put("list", list)
                    .put("current_page", pageUtils.getCurrPage())
                    .put("page_size", pageUtils.getPageSize())
                    .put("total_page", pageUtils.getTotalPage())
                    .put("total_count", pageUtils.getTotalCount());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @SysLog("???????????????")
    public R v1_1_parse_taocode(SysMember sysMember, String plain) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> map = (Map<String, Object>) objectMapper.readValue(plain, Map.class);

            String taocode = (String) map.get("taocode");

            List<TaobaoAccountEntity> activeAccounts = this.taobaoAccountService.getActivesByMember(null, Config.MAX_RETRY_ACCOUNTS);
            R r = R.error();
            for (int retry = 0; activeAccounts != null && retry < activeAccounts.size(); retry++) {
                r = this.taobaoApiService.getLiveInfo(taocode, activeAccounts.get(retry));
                if (r.getCode() == ErrorCodes.SUCCESS)
                    break;
            }

            if (activeAccounts == null) {
                return R.error("????????????????????????");
            }
            return r;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @SysLog("???????????????????????????")
    public R v1_1_get_live_entry(SysMember sysMember, String plain) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> map = (Map<String, Object>) objectMapper.readValue(plain, Map.class);

            String liveId = (String) map.get("live_id");
            String accountId = (String) map.get("account_id");

            LiveRoomEntity liveRoomEntity = new LiveRoomEntity();
            liveRoomEntity.setLiveId(liveId);
            liveRoomEntity.setAccountId(accountId);

            List<TaobaoAccountEntity> activeAccounts = this.taobaoAccountService.getActivesByMember(null, Config.MAX_RETRY_ACCOUNTS);
            for (int retry = 0; activeAccounts != null && retry < activeAccounts.size(); retry++) {
                this.taobaoApiService.getH5Token(activeAccounts.get(retry));
                R r = this.taobaoApiService.getRankingListData(liveRoomEntity, activeAccounts.get(retry));
                if (r.getCode() == ErrorCodes.SUCCESS)
                    break;
            }


            return R.ok()
                    .put("has_ranking_entry", liveRoomEntity.isHasRankingListEntry())
                    .put("ranking_score", liveRoomEntity.getRankingListData().getRankingScore())
                    .put("ranking_num", liveRoomEntity.getRankingListData().getRankingNum())
                    .put("ranking_name", liveRoomEntity.getRankingListData().getRankingName());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @SysLog("????????????????????????")
    public R v1_1_limit_score(SysMember sysMember, String plain) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> map = (Map<String, Object>) objectMapper.readValue(plain, Map.class);

            String liveId = (String) map.get("live_id");
            boolean hasFollow = (boolean) map.get("has_follow");
            boolean hasStay = (boolean) map.get("has_stay");
            boolean hasBuy = (boolean) map.get("has_buy");
            boolean hasDoubleBuy = (boolean) map.get("has_double_buy");

            List<TaobaoAccountEntity> taobaoAccountEntities = this.rankingService.availableAccounts(sysMember, liveId);

            int unitScore = hasDoubleBuy ? this.rankingService.getRankingUnitScore(RankingScore.DoubleBuy) :
                    (hasBuy ? this.rankingService.getRankingUnitScore(RankingScore.Buy) : 0);
            unitScore += hasFollow ? this.rankingService.getRankingUnitScore(RankingScore.Follow) : 0;
            unitScore += hasStay ? this.rankingService.getRankingUnitScore(RankingScore.Stay) : 0;

            int count = taobaoAccountEntities == null || taobaoAccountEntities.size() < 1 ? 0 : taobaoAccountEntities.size();

            return R.ok().put("limit_score", unitScore * count);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @SysLog("???????????????")
    /**
     * ?????????????????????
     * @param sysMember
     * @param plain
     * @return
     */
    public R v1_1_start_task(SysMember sysMember, String plain) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            AddRankingTaskParam2 param = (AddRankingTaskParam2) objectMapper.readValue(plain, AddRankingTaskParam2.class);

            String taocode = param.getTaocode();
            String liveId = param.getLiveId();
            String accountId = param.getAccountId();
            String roomName = param.getRoomName();
            int targetScore = param.getTargetScore();
            boolean hasFollow = param.isHasFollow();
            boolean hasStay = param.isHasStay();
            boolean hasBuy = param.isHasBuy();
            boolean hasDoubleBuy = param.isHasDoubleBuy();
            Date startTime = param.getStartTime();
            String comment = param.getComment();

            LiveRoomEntity liveRoomEntity = new LiveRoomEntity();

            liveRoomEntity.setLiveId(liveId);
            liveRoomEntity.setAccountName(roomName);

            RankingEntity rankingEntity = this.rankingService.addNewTask(sysMember,
                    taocode,
                    liveId, accountId, roomName,
                    targetScore,
                    hasFollow, hasStay, hasBuy, hasDoubleBuy,
                    startTime, comment);

            if (rankingEntity == null) {
                return R.error("???????????????");
            }

            return R.ok().put("task_id", rankingEntity.getId());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @SysLog("???????????????")
    public R v1_1_stop_task(SysMember sysMember, String plain) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> map = (Map<String, Object>) objectMapper.readValue(plain, Map.class);

            int taskId = (int) map.get("task_id");

            RankingEntity rankingEntity = this.rankingService.getOne(Wrappers.<RankingEntity>lambdaQuery()
                    .eq(RankingEntity::getId, taskId));

            if (rankingEntity == null) {
                return R.error("???????????????");
            }

            this.rankingService.stopTask(rankingEntity);

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @SysLog("??????????????????")
    public R v1_1_get_status(SysMember sysMember, String plain) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> map = (Map<String, Object>) objectMapper.readValue(plain, Map.class);

            int taskId = (int) map.get("task_id");

            RankingEntity rankingEntity = this.rankingService.getOne(Wrappers.<RankingEntity>lambdaQuery()
                    .eq(RankingEntity::getId, taskId));

            return R.ok().put("ranking", rankingEntity);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }
}
