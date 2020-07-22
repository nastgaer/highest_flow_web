package highest.flow.taobaolive.external.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import highest.flow.taobaolive.api.param.AddRankingTaskParam;
import highest.flow.taobaolive.api.param.PageParam;
import highest.flow.taobaolive.common.defines.ErrorCodes;
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
import highest.flow.taobaolive.taobao.entity.LiveRoomEntity;
import highest.flow.taobaolive.taobao.entity.RankingEntity;
import highest.flow.taobaolive.taobao.entity.TaobaoAccountEntity;
import highest.flow.taobaolive.taobao.service.RankingService;
import highest.flow.taobaolive.taobao.service.TaobaoAccountService;
import highest.flow.taobaolive.taobao.service.TaobaoApiService;
import org.aspectj.apache.bcel.generic.RET;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
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
                return R.error(ErrorCodes.NOT_FOUND_REMOTE_FUNC, "不正确的函数");
            }

            String data = param.getData();
            String sign = param.getSign();

            boolean verify = cryptoService.verify(data, sign);
            if (!verify) {
                return R.error("参数验证失败");
            }

            String plain = cryptoService.decrypt(data);

            R r = callApi(param.getVersion(),
                    param.getApi(),
                    plain);

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
                return R.error("参数验证失败");
            }

            String plain = cryptoService.decrypt(data);

            R r = callApi(param.getVersion(),
                    param.getApi(),
                    plain);

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
                return R.error(ErrorCodes.NOT_FOUND_USER, "找不到用户");
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
        return R.error(ErrorCodes.NOT_FOUND_REMOTE_FUNC, "找不到方法");
    }

    public R v1_1_register_code(SysMember member, String plain) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> map = (Map<String, Object>) objectMapper.readValue(plain, Map.class);

            String code = (String) map.get("code");
            String machineCode = (String) map.get("machineCode");

            LicenseCode licenseCode = licenseCodeService.getCodeDesc(code);
            if (licenseCode == null) {
                return R.error(ErrorCodes.NOT_FOUND_CODE, "找不到卡密");
            }

            if (licenseCode.getState() == LicenseCodeState.Deleted.getState()) {
                return R.error(ErrorCodes.INVALID_CODE, "找不到卡密");
            }

            SysMember sysMember = null;
            if (licenseCode.getState() == LicenseCodeState.Created.getState()) {
                // 根据卡密类型，创建用户
                sysMember = this.memberService.createLicenseMember(LicenseCodeType.from(licenseCode.getCodeType()),
                        MemberServiceType.from(licenseCode.getServiceType()));

                licenseCode.setMachineCode(machineCode);
                licenseCode.setState(LicenseCodeState.Accepted.getState());
                licenseCode.setMemberId(sysMember.getId());
                Date serviceStartTime = new Date();
                Calendar calender = Calendar.getInstance();
                calender.setTime(serviceStartTime);
                calender.add(Calendar.HOUR, licenseCode.getHours());
                Date serviceEndTime = calender.getTime();
                licenseCode.setServiceStartTime(serviceStartTime);
                licenseCode.setServiceEndTime(serviceEndTime);

                licenseCode.setAcceptedTime(new Date());
                this.licenseCodeService.updateById(licenseCode);

            } else {
                sysMember = this.memberService.getOne(Wrappers.<SysMember>lambdaQuery().eq(SysMember::getId, licenseCode.getMemberId()));

            }

            if (sysMember == null) {
                return R.error(ErrorCodes.NOT_FOUND_USER, "找不到账号");
            }

            if (licenseCode.getMachineCode().compareTo(machineCode) != 0) {
                return R.error(ErrorCodes.UNAUTHORIZED_MACHINE, "不是绑定的机器");
            }

            if (sysMember.getState() != MemberState.Normal.getState()) {
                return R.error(ErrorCodes.UNALLOWED_USER, "账号已被停用，请联系管理员");
            }

            // 登录
            R r = memberTokenService.createToken(sysMember.getId());
            String accessToken = (String) r.get("access_token");

            return R.ok()
                    .put("access_token", accessToken)
                    .put("service_start_time", licenseCode.getServiceStartTime())
                    .put("service_end_time", licenseCode.getServiceEndTime());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    /**
     * 下载小号列表
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

    public R v1_1_parse_taocode(SysMember sysMember, String plain) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> map = (Map<String, Object>) objectMapper.readValue(plain, Map.class);

            String taocode = (String) map.get("taocode");

            TaobaoAccountEntity taobaoAccountEntity = this.taobaoAccountService.getActiveOne(getUser());
            if (taobaoAccountEntity == null) {
                return R.error("找不到活跃的用户");
            }

            R r = taobaoApiService.getLiveInfo(taocode, taobaoAccountEntity);
            return r;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    /**
     * 开始刷热度任务
     * @param sysMember
     * @param plain
     * @return
     */
    public R v1_1_start_task(SysMember sysMember, String plain) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            AddRankingTaskParam param = (AddRankingTaskParam) objectMapper.readValue(plain, AddRankingTaskParam.class);

            String taocode = param.getTaocode();
            int targetScore = param.getTargetScore();
            boolean doubleBuy = param.isDoubleBuy();
            Date startTime = param.getStartTime();

            TaobaoAccountEntity taobaoAccountEntity = this.taobaoAccountService.getActiveOne(getUser());
            if (taobaoAccountEntity == null) {
                return R.error("找不到活跃的用户");
            }

            R r = taobaoApiService.getLiveInfo(taocode, taobaoAccountEntity);
            if (r.getCode() != ErrorCodes.SUCCESS) {
                return r;
            }

            LiveRoomEntity liveRoomEntity = (LiveRoomEntity) r.get("live_room");

            RankingEntity rankingEntity = this.rankingService.addNewTask(sysMember,
                    liveRoomEntity,
                    taocode,
                    targetScore,
                    doubleBuy,
                    startTime);

            if (rankingEntity == null) {
                return R.error("找不到任务");
            }

            return R.ok().put("task_id", rankingEntity.getId());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    public R v1_1_stop_task(SysMember sysMember, String plain) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> map = (Map<String, Object>) objectMapper.readValue(plain, Map.class);

            int taskId = (int) map.get("task_id");

            RankingEntity rankingEntity = this.rankingService.getOne(Wrappers.<RankingEntity>lambdaQuery()
                    .eq(RankingEntity::getId, taskId));

            if (rankingEntity == null) {
                return R.error("找不到任务");
            }

            this.rankingService.stopTask(rankingEntity);

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

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