package highest.flow.taobaolive.taobao.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import highest.flow.taobaolive.api.param.AddRoomParam;
import highest.flow.taobaolive.common.utils.CommonUtils;
import highest.flow.taobaolive.common.utils.DateUtils;
import highest.flow.taobaolive.common.utils.HFStringUtils;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.sys.controller.AbstractController;
import highest.flow.taobaolive.api.param.PageParam;
import highest.flow.taobaolive.sys.entity.SysMember;
import highest.flow.taobaolive.taobao.defines.ServiceState;
import highest.flow.taobaolive.taobao.entity.*;
import highest.flow.taobaolive.taobao.service.*;
import org.apache.shiro.crypto.hash.Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/v1.0/live")
public class LiveController extends AbstractController {

    @Autowired
    private TaobaoAccountService taobaoAccountService;

    @Autowired
    private TaobaoApiService taobaoApiService;

    @Autowired
    private LiveService liveService;

    @Autowired
    private MemberTaoAccService memberTaoAccService;

    @Autowired
    private PreLiveRoomSpecService preLiveRoomSpecService;

    @Autowired
    private LiveRoomStrategyService liveRoomStrategyService;

    @GetMapping("/columns")
    public R getLiveColumns() {
        try {
            List<LiveChannel> channels = liveService.getChannels();

            return R.ok().put("channels", channels);

        } catch (Exception ex) {
            ex.printStackTrace();;
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
    public R uploadImage(@RequestBody MultipartFile file) {
        try {
            // TODO

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @PostMapping("/add_room")
    public R addRoom(@RequestBody AddRoomParam addRoomParam) {
        try {
            TaobaoAccountEntity taobaoAccountEntity = taobaoAccountService.getOne(Wrappers.<TaobaoAccountEntity>lambdaQuery()
                    .eq(TaobaoAccountEntity::getNick, addRoomParam.getTaobaoAccountNick()));
            if (taobaoAccountEntity == null) {
                return R.error("找不到用户");
            }

            MemberTaoAccEntity memberTaoAccEntity = this.memberTaoAccService.getOne(Wrappers.<MemberTaoAccEntity>lambdaQuery()
                    .eq(MemberTaoAccEntity::getTaobaoAccountNick, addRoomParam.getTaobaoAccountNick()));
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
            int pageNo = pageParam.getPageNo();
            int pageSize = pageParam.getPageSize();
            String keyword = pageParam.getKeyword();

            IPage<MemberTaoAccEntity> page = HFStringUtils.isNullOrEmpty(keyword) ?
                    this.memberTaoAccService.page(new Page<>((pageNo - 1) * pageSize, pageSize)) :
                    this.memberTaoAccService.page(new Page<>((pageNo - 1) * pageSize, pageSize),
                            Wrappers.<MemberTaoAccEntity>lambdaQuery()
                                    .like(MemberTaoAccEntity::getRoomName, keyword)
                                    .or()
                                    .like(MemberTaoAccEntity::getTaobaoAccountNick, keyword));
            List<MemberTaoAccEntity> memberTaoAccEntities = page.getRecords();

            for (MemberTaoAccEntity memberTaoAccEntity : memberTaoAccEntities) {
                String taobaoAccountNick = memberTaoAccEntity.getTaobaoAccountNick();

                List<PreLiveRoomSpecEntity> preLiveRoomSpecEntities = this.preLiveRoomSpecService.list(Wrappers.<PreLiveRoomSpecEntity>lambdaQuery()
                        .eq(PreLiveRoomSpecEntity::getTaobaoAccountNick, taobaoAccountNick));

                memberTaoAccEntity.setPreLiveRoomSpecs(preLiveRoomSpecEntities);
            }

            return R.ok()
                    .put("rooms", memberTaoAccEntities)
                    .put("total_count", this.memberTaoAccService.count());

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

            MemberTaoAccEntity memberTaoAccEntity = this.memberTaoAccService.getOne(Wrappers.<MemberTaoAccEntity>lambdaQuery()
                    .eq(MemberTaoAccEntity::getTaobaoAccountNick, taobaoAccountNick));

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

            MemberTaoAccEntity memberTaoAccEntity = this.memberTaoAccService.getOne(Wrappers.<MemberTaoAccEntity>lambdaQuery()
                    .eq(MemberTaoAccEntity::getTaobaoAccountNick, taobaoAccountNick));

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

            MemberTaoAccEntity memberTaoAccEntity = this.memberTaoAccService.getOne(Wrappers.<MemberTaoAccEntity>lambdaQuery()
                    .eq(MemberTaoAccEntity::getTaobaoAccountNick, taobaoAccountNick));

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
            int pageNo = pageParam.getPageNo();
            int pageSize = pageParam.getPageSize();
            String keyword = pageParam.getKeyword();

            IPage<LiveRoom> page =
                    HFStringUtils.isNullOrEmpty(keyword) ?
                            this.rankingService
                                    .page(new Page<>((pageNo - 1) * pageSize, pageSize)) :
                            this.rankingService
                                    .page(new Page<>((pageNo - 1) * pageSize, pageSize),
                                            Wrappers.<RankingEntity>lambdaQuery().like(RankingEntity::getRoomName, keyword));
            List<RankingEntity> logs = page.getRecords();
            return R.ok().put("logs", logs).put("total_count", rankingService.count());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @PostMapping("/set_task")
    public R setTask(@RequestBody Map<String, Object> params) {
        try {
            String taobaoAccountNick = (String) params.get("taobao_account_nick");
            List<LiveRoomStrategyEntity> liveRoomStrategyEntities = (List<LiveRoomStrategyEntity>) params.get("prelives");

            TaobaoAccountEntity taobaoAccountEntity = taobaoAccountService.getOne(Wrappers.<TaobaoAccountEntity>lambdaQuery().eq(TaobaoAccountEntity::getNick,
                    taobaoAccountNick));
            if (taobaoAccountEntity == null) {
                return R.error("找不到用户");
            }

            for (LiveRoomStrategyEntity liveRoomStrategyEntity : liveRoomStrategyEntities) {
                liveRoomStrategyEntity.setCreatedTime(new Date());
                liveRoomStrategyEntity.setUpdatedTime(new Date());
            }

            liveRoomStrategyService.saveBatch(liveRoomStrategyEntities);

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }
}
