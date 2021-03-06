package highest.flow.taobaolive.taobao.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import highest.flow.taobaolive.api.param.AddRoomParam;
import highest.flow.taobaolive.api.param.SetLiveRoomStrategyParam;
import highest.flow.taobaolive.common.annotation.SysLog;
import highest.flow.taobaolive.common.config.Config;
import highest.flow.taobaolive.common.defines.ErrorCodes;
import highest.flow.taobaolive.common.utils.*;
import highest.flow.taobaolive.sys.controller.AbstractController;
import highest.flow.taobaolive.api.param.PageParam;
import highest.flow.taobaolive.sys.entity.SysMember;
import highest.flow.taobaolive.taobao.defines.ServiceState;
import highest.flow.taobaolive.taobao.defines.TaobaoAccountState;
import highest.flow.taobaolive.taobao.entity.*;
import highest.flow.taobaolive.taobao.service.*;
import org.apache.shiro.crypto.hash.Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/v1.0/live")
public class LiveController extends AbstractController {

    @Value("${upload.folder:./}")
    private String uploadFolder;

    @Autowired
    private TaobaoAccountService taobaoAccountService;

    @Autowired
    private TaobaoApiService taobaoApiService;

    @Autowired
    private LiveService liveService;

    @Autowired
    private MemberTaoAccService memberTaoAccService;

    @Autowired
    private LiveRoomService liveRoomService;

    @Autowired
    private PreLiveRoomSpecService preLiveRoomSpecService; // 直播间基本信息

    @Autowired
    private LiveRoomStrategyService liveRoomStrategyService;

    @Autowired
    private LiveRoomProductService liveRoomProductService;

    @PostMapping("/columns")
    public R getLiveColumns() {
        try {
            List<LiveChannel> channels = liveService.getChannels();

            return R.ok().put("channels", channels);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    //@SysLog("解析淘口令")
//    @PostMapping("/parse_taocode")
//    public R parseTaoCode(@RequestBody Map<String, Object> params) {
//        try {
//            String taocode = (String) params.get("taocode");
//
//            return taobaoApiService.getLiveInfo(taocode, null);
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return R.error("解析淘口令失败");
//    }

    @PostMapping("/get_live_info")
    public R getLiveInfo(@RequestBody Map<String, Object> params) {
        try {
            String liveId = (String) params.get("live_id");

            LiveRoomEntity liveRoomEntity = new LiveRoomEntity();
            liveRoomEntity.setLiveId(liveId);
            liveRoomEntity.setCreatorId("");
            liveRoomEntity.setTalentLiveUrl("");

            TaobaoAccountEntity tempAccount = new TaobaoAccountEntity();
            taobaoApiService.getH5Token(tempAccount);
            R r = taobaoApiService.getLiveDetail(liveId, tempAccount);

            //R r = taobaoApiService.getLivePreGet(liveId);
            if (r.getCode() != ErrorCodes.SUCCESS) {
                return r;
            }

            liveRoomEntity.setAccountId((String) r.get("account_id"));
            liveRoomEntity.setAccountName((String) r.get("account_name"));
            liveRoomEntity.setFansNum((int) r.get("fans_num"));
            liveRoomEntity.setTopic((String) r.get("topic"));
            liveRoomEntity.setViewCount((int) r.get("view_count"));
            liveRoomEntity.setPraiseCount((int) r.get("praise_count"));
            liveRoomEntity.setOnlineCount((int) r.get("online_count"));
            liveRoomEntity.setLiveCoverImg((String) r.get("cover_img"));
            liveRoomEntity.setLiveCoverImg169((String) r.get("cover_img169"));
            liveRoomEntity.setLiveTitle((String) r.get("title"));
            liveRoomEntity.setLiveIntro((String) r.get("intro"));
            liveRoomEntity.setLiveChannelId((int) r.get("channel_id"));
            liveRoomEntity.setLiveColumnId((int) r.get("column_id"));
            liveRoomEntity.setLiveLocation((String) r.get("location"));

            List<TaobaoAccountEntity> activeAccounts = this.taobaoAccountService.getActivesByMember(null, Config.MAX_RETRY_ACCOUNTS);
            for (int retry = 0; activeAccounts != null && retry < activeAccounts.size(); retry++) {
                TaobaoAccountEntity taobaoAccountEntity = activeAccounts.get(retry);
                this.taobaoApiService.getH5Token(taobaoAccountEntity);
                r = this.taobaoApiService.getLiveEntry(liveRoomEntity, taobaoAccountEntity);
                if (r.getCode() == ErrorCodes.SUCCESS)
                    break;
            }

            return R.ok().put("live_room", liveRoomEntity);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error("解析淘口令失败");
    }

    @PostMapping("/get_live_entry")
    public R getLiveEntry(@RequestBody Map<String, Object> params) {
        try {
            String liveId = (String) params.get("live_id");
            String accountId = (String) params.get("account_id");

            LiveRoomEntity liveRoomEntity = new LiveRoomEntity();
            liveRoomEntity.setLiveId(liveId);
            liveRoomEntity.setAccountId(accountId);

            List<TaobaoAccountEntity> activeAccounts = this.taobaoAccountService.getActivesByMember(null, Config.MAX_RETRY_ACCOUNTS);
            for (int retry = 0; activeAccounts != null && retry < activeAccounts.size(); retry++) {
                TaobaoAccountEntity taobaoAccountEntity = activeAccounts.get(retry);
                this.taobaoApiService.getH5Token(taobaoAccountEntity);
                R r = this.taobaoApiService.getLiveEntry(liveRoomEntity, taobaoAccountEntity);
                if (r.getCode() == ErrorCodes.SUCCESS)
                    break;
            }

            return R.ok().put("live_room", liveRoomEntity);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error("解析淘口令失败");
    }

    @PostMapping("/upload_image")
    public R uploadImage(@RequestParam(name = "taobao_account_nick") String taobaoAccountNick,
                         @RequestParam(name = "file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return R.error("空文件");
            }

            TaobaoAccountEntity taobaoAccountEntity = taobaoAccountService.getInfo(taobaoAccountNick);
            if (taobaoAccountEntity == null) {
                return R.error("找不到淘宝账号");
            }

            if (taobaoAccountEntity.getState() != TaobaoAccountState.Normal.getState()) {
                return R.error("不是正常账号");
            }

            byte [] bytes = file.getBytes();
            Path path = Paths.get(uploadFolder, file.getOriginalFilename());
            Files.write(path, bytes);

            R r = this.taobaoApiService.uploadImage(path, taobaoAccountEntity);

            ObjectMapper objectMapper = new ObjectMapper();
            logger.info(taobaoAccountNick + "：上传封面图，" + objectMapper.writeValueAsString(r));

            return r;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @SysLog("注册直播间")
    @PostMapping("/add_room")
    public R addRoom(@RequestBody AddRoomParam addRoomParam) {
        try {
            TaobaoAccountEntity taobaoAccountEntity = taobaoAccountService.getInfo(addRoomParam.getTaobaoAccountNick());
            if (taobaoAccountEntity == null) {
                return R.error("找不到用户");
            }

            MemberTaoAccEntity memberTaoAccEntity = this.memberTaoAccService.getMemberByTaobaoAccountNick(addRoomParam.getTaobaoAccountNick());
            if (memberTaoAccEntity != null) {
                return R.error("已经注册的直播间");
            }

            SysMember sysMember = getUser();

            // 第一次注册直播间
            memberTaoAccEntity = new MemberTaoAccEntity();
            memberTaoAccEntity.setMemberId(sysMember.getId());
            memberTaoAccEntity.setTaobaoAccountNick(taobaoAccountEntity.getNick());
            memberTaoAccEntity.setRoomName("");
            memberTaoAccEntity.setComment(addRoomParam.getComment());

            Date startDate = addRoomParam.getService().getStartDate();
            Date endDate = CommonUtils.addDays(startDate, addRoomParam.getService().getDays());
            memberTaoAccEntity.setServiceStartDate(startDate);
            memberTaoAccEntity.setServiceEndDate(endDate);
            if (startDate.getTime() > new Date().getTime()) {
                memberTaoAccEntity.setState(ServiceState.Waiting.getState());
            } else if (endDate.getTime() < new Date().getTime()) {
                memberTaoAccEntity.setState(ServiceState.Stopped.getState());
            } else {
                memberTaoAccEntity.setState(ServiceState.Normal.getState());
            }

            memberTaoAccEntity.setOperationStartTime(new Date());

            memberTaoAccEntity.setCreatedTime(new Date());
            memberTaoAccEntity.setUpdatedTime(new Date());
            this.memberTaoAccService.save(memberTaoAccEntity);

            for (PreLiveRoomSpecEntity preLiveRoomSpecEntity : addRoomParam.getLiveSpecs()) {
                preLiveRoomSpecEntity.setTaobaoAccountNick(taobaoAccountEntity.getNick());
                preLiveRoomSpecEntity.setCreatedTime(new Date());
                preLiveRoomSpecEntity.setUpdatedTime(new Date());
            }

            this.preLiveRoomSpecService.saveBatch(addRoomParam.getLiveSpecs());

            return R.ok().put("room_id", memberTaoAccEntity.getId());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @SysLog("直播间列表")
    @PostMapping("/list")
    public R list(@RequestBody PageParam pageParam) {
        try {
            PageUtils pageUtils = this.memberTaoAccService.queryPage(pageParam);

            List<MemberTaoAccEntity> memberTaoAccEntities = pageUtils.getList();

            for (MemberTaoAccEntity memberTaoAccEntity : memberTaoAccEntities) {
                String taobaoAccountNick = memberTaoAccEntity.getTaobaoAccountNick();

                List<PreLiveRoomSpecEntity> preLiveRoomSpecEntities = this.preLiveRoomSpecService.getPreLiveRoomSpecs(taobaoAccountNick);

                memberTaoAccEntity.setPreLiveRoomSpecs(preLiveRoomSpecEntities);
            }

            return R.ok()
                    .put("rooms", memberTaoAccEntities)
                    .put("total_count", pageUtils.getTotalCount());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @SysLog("续费服务")
    @PostMapping("/recharge")
    public R recharge(@RequestBody Map<String, Object> params) {
        try {
            String taobaoAccountNick = (String) params.get("taobao_account_nick");
            int days = Integer.parseInt(String.valueOf(params.get("days")));

            MemberTaoAccEntity memberTaoAccEntity = this.memberTaoAccService.getMemberByTaobaoAccountNick(taobaoAccountNick);

            if (memberTaoAccEntity == null) {
                return R.error("还没注册的直播间");
            }

            Date startDate = memberTaoAccEntity.getServiceStartDate();
            Date endDate = memberTaoAccEntity.getServiceEndDate();

            if (endDate.getTime() < new Date().getTime()) {
                startDate = new Date();
                endDate = new Date();
            }

            endDate = CommonUtils.addDays(endDate, days);

            memberTaoAccEntity.setServiceStartDate(startDate);
            memberTaoAccEntity.setServiceEndDate(endDate);
            memberTaoAccEntity.setState(ServiceState.Normal.getState());

            this.memberTaoAccService.updateById(memberTaoAccEntity);

            this.liveRoomStrategyService.resumeTask(memberTaoAccEntity);

            return R.ok()
                    .put("start_date", startDate)
                    .put("end_date", endDate);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @SysLog("停止服务")
    @PostMapping("/stop_service")
    public R stopService(@RequestBody Map<String, Object> params) {
        try {
            String taobaoAccountNick = (String) params.get("taobao_account_nick");

            MemberTaoAccEntity memberTaoAccEntity = this.memberTaoAccService.getMemberByTaobaoAccountNick(taobaoAccountNick);

            if (memberTaoAccEntity == null) {
                return R.error("还没注册的直播间");
            }

            if (memberTaoAccEntity.getState() != ServiceState.Normal.getState()) {
                return R.error("已停止服务");
            }

            memberTaoAccEntity.setState(ServiceState.Suspended.getState());

            this.memberTaoAccService.updateById(memberTaoAccEntity);

            this.liveRoomStrategyService.stopTask(memberTaoAccEntity);

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @SysLog("继续服务")
    @PostMapping("/resume_service")
    public R resumeService(@RequestBody Map<String, Object> params) {
        try {
            String taobaoAccountNick = (String) params.get("taobao_account_nick");

            MemberTaoAccEntity memberTaoAccEntity = this.memberTaoAccService.getMemberByTaobaoAccountNick(taobaoAccountNick);

            if (memberTaoAccEntity == null) {
                return R.error("还没注册的直播间");
            }

            if (memberTaoAccEntity.getServiceEndDate().getTime() < new Date().getTime()) {
                return R.error("已停止服务的直播间");
            }

            memberTaoAccEntity.setState(ServiceState.Normal.getState());

            this.memberTaoAccService.updateById(memberTaoAccEntity);

            this.liveRoomStrategyService.resumeTask(memberTaoAccEntity);

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @PostMapping("/logs")
    public R logs(@RequestBody PageParam pageParam) {
        try {
            PageUtils pageUtils = this.liveRoomService.queryPage(pageParam);

            List<LiveRoomEntity> liveRoomEntities = pageUtils.getList();
            for (LiveRoomEntity liveRoomEntity : liveRoomEntities) {
                List<ProductEntity> productEntities = this.liveRoomProductService.getProducts(liveRoomEntity);
                liveRoomEntity.setProducts(productEntities);
            }

            return R.ok().put("logs", pageUtils.getList()).put("total_count", pageUtils.getTotalCount());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @SysLog("获取直播间信息")
    @PostMapping("/get_task")
    public R getTask(@RequestBody Map<String, Object> params) {
        try {
            String taobaoAccountNick = (String) params.get("taobao_account_nick");

            MemberTaoAccEntity memberTaoAccEntity = this.memberTaoAccService.getMemberByTaobaoAccountNick(taobaoAccountNick);

            if (memberTaoAccEntity == null) {
                return R.error("没注册的直播间");
            }

            List<PreLiveRoomSpecEntity> preLiveRoomSpecEntities = this.preLiveRoomSpecService.getPreLiveRoomSpecs(taobaoAccountNick);

            memberTaoAccEntity.setPreLiveRoomSpecs(preLiveRoomSpecEntities);

            List<LiveRoomStrategyEntity> strategyEntities = this.liveRoomStrategyService.getLiveRoomStrategies(memberTaoAccEntity);

            memberTaoAccEntity.setLiveRoomStrategies(strategyEntities);

            return R.ok().put("task", memberTaoAccEntity);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @SysLog("引流操作")
    @PostMapping("/set_task")
    public R setTask(@RequestBody SetLiveRoomStrategyParam setLiveRoomStrategyParam) {
        try {
            String taobaoAccountNick = setLiveRoomStrategyParam.getTaobaoAccountNick();
            List<LiveRoomStrategyEntity> liveRoomStrategyEntities = (List<LiveRoomStrategyEntity>) setLiveRoomStrategyParam.getLiveRoomStrategies();

            MemberTaoAccEntity memberTaoAccEntity = this.memberTaoAccService.getMemberByTaobaoAccountNick(taobaoAccountNick);

            if (memberTaoAccEntity == null) {
                return R.error("还没注册的直播间");
            }

            TaobaoAccountEntity taobaoAccountEntity = taobaoAccountService.getInfo(taobaoAccountNick);
            if (taobaoAccountEntity == null) {
                return R.error("找不到用户");
            }

            memberTaoAccEntity.setOperationStartTime(setLiveRoomStrategyParam.getOperationStartTime());
            this.memberTaoAccService.updateById(memberTaoAccEntity);

            boolean success = this.liveRoomStrategyService.setTask(memberTaoAccEntity, liveRoomStrategyEntities);

            if (success) {
                return R.ok();
            } else {
                return R.error();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }
}
