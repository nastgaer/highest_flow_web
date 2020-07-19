package highest.flow.taobaolive.taobao.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import highest.flow.taobaolive.api.param.AddRoomParam;
import highest.flow.taobaolive.api.param.SetLiveRoomStrategyParam;
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
    private PreLiveRoomSpecService preLiveRoomSpecService;

    @Autowired
    private LiveRoomStrategyService liveRoomStrategyService;

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

    @PostMapping("/parse_taocode")
    public R parseTaoCode(@RequestBody Map<String, Object> params) {
        try {
            String taocode = (String) params.get("taocode");

            return taobaoApiService.getLiveInfo(taocode, null);

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

            return r;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

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

    @PostMapping("/list")
    public R list(@RequestBody PageParam pageParam) {
        try {
            PageUtils pageUtils = this.memberTaoAccService.queryPage(pageParam);

            List<MemberTaoAccEntity> memberTaoAccEntities = pageUtils.getList();

            for (MemberTaoAccEntity memberTaoAccEntity : memberTaoAccEntities) {
                String taobaoAccountNick = memberTaoAccEntity.getTaobaoAccountNick();

                List<PreLiveRoomSpecEntity> preLiveRoomSpecEntities = this.preLiveRoomSpecService.list(Wrappers.<PreLiveRoomSpecEntity>lambdaQuery()
                        .eq(PreLiveRoomSpecEntity::getTaobaoAccountNick, taobaoAccountNick));

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

    @PostMapping("/recharge")
    public R recharge(@RequestBody Map<String, Object> params) {
        try {
            String taobaoAccountNick = (String) params.get("taobao_account_nick");
            int days = Integer.parseInt(String.valueOf(params.get("days")));

            MemberTaoAccEntity memberTaoAccEntity = this.memberTaoAccService.getMemberByTaobaoAccountNick(taobaoAccountNick);

            if (memberTaoAccEntity != null) {
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

            this.memberTaoAccService.save(memberTaoAccEntity);

            return R.ok()
                    .put("start_date", startDate)
                    .put("end_date", endDate);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @PostMapping("/stop_service")
    public R stopService(@RequestBody Map<String, Object> params) {
        try {
            String taobaoAccountNick = (String) params.get("taobao_account_nick");

            MemberTaoAccEntity memberTaoAccEntity = this.memberTaoAccService.getMemberByTaobaoAccountNick(taobaoAccountNick);

            if (memberTaoAccEntity != null) {
                return R.error("还没注册的直播间");
            }

            if (memberTaoAccEntity.getState() != ServiceState.Normal.getState()) {
                return R.error("已停止服务");
            }

            memberTaoAccEntity.setState(ServiceState.Suspended.getState());

            this.memberTaoAccService.save(memberTaoAccEntity);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @PostMapping("/resume_service")
    public R resumeService(@RequestBody Map<String, Object> params) {
        try {
            String taobaoAccountNick = (String) params.get("taobao_account_nick");

            MemberTaoAccEntity memberTaoAccEntity = this.memberTaoAccService.getMemberByTaobaoAccountNick(taobaoAccountNick);

            if (memberTaoAccEntity != null) {
                return R.error("还没注册的直播间");
            }

            if (memberTaoAccEntity.getServiceEndDate().getTime() < new Date().getTime()) {
                return R.error("已停止服务的直播间");
            }

            memberTaoAccEntity.setState(ServiceState.Suspended.getState());

            this.memberTaoAccService.save(memberTaoAccEntity);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @PostMapping("/logs")
    public R logs(@RequestBody PageParam pageParam) {
        try {
            PageUtils pageUtils = this.liveRoomService.queryPage(pageParam);

            return R.ok().put("logs", pageUtils.getList()).put("total_count", pageUtils.getTotalCount());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @PostMapping("/set_task")
    public R setTask(@RequestBody SetLiveRoomStrategyParam setLiveRoomStrategyParam) {
        try {
            String taobaoAccountNick = setLiveRoomStrategyParam.getTaobaoAccountNick();
            List<LiveRoomStrategyEntity> liveRoomStrategyEntities = (List<LiveRoomStrategyEntity>) setLiveRoomStrategyParam.getPrelives();

            MemberTaoAccEntity memberTaoAccEntity = this.memberTaoAccService.getMemberByTaobaoAccountNick(taobaoAccountNick);

            if (memberTaoAccEntity != null) {
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
                return R.error();
            } else {
                return R.ok();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }
}
