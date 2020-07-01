package highest.flow.taobaolive.taobao.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.sys.controller.AbstractController;
import highest.flow.taobaolive.sys.entity.PageEntity;
import highest.flow.taobaolive.sys.entity.ServiceEntity;
import highest.flow.taobaolive.sys.entity.SysMember;
import highest.flow.taobaolive.taobao.entity.*;
import highest.flow.taobaolive.taobao.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/live")
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

    @PostMapping("/add_room")
    public R addRoom(@RequestParam(name = "taobao_account_id") String taobaoAccountId,
                     @RequestParam(name = "comment") String comment,
                     @RequestParam(name = "service") ServiceEntity serviceEntity,
                     @RequestParam(name="live_spec[]") PreLiveRoomSpecEntity[] preLiveRoomSpecEntities) {
        try {
            TaobaoAccountEntity taobaoAccountEntity = taobaoAccountService.getOne(Wrappers.<TaobaoAccountEntity>lambdaQuery().eq(TaobaoAccountEntity::getTaobaoAccountId, taobaoAccountId));
            if (taobaoAccountEntity == null) {
                return R.error("找不到用户");
            }

            SysMember sysMember = getUser();

            MemberTaoAccEntity memberTaoAccEntity = memberTaoAccService.getOne(Wrappers.<MemberTaoAccEntity>lambdaQuery().eq(SysMember::getId, sysMember.getId()).eq(TaobaoAccountEntity::getAccountId, taobaoAccountEntity.getAccountId));
            if (memberTaoAccEntity == null) {
                // 第一次注册直播间
                memberTaoAccEntity = new MemberTaoAccEntity();
                memberTaoAccEntity.setMemberId(sysMember.getId());
                memberTaoAccEntity.setTaobaoAccountId(taobaoAccountEntity.getAccountId());
                memberTaoAccEntity.setRoomName("");
                memberTaoAccEntity.setTaocode("");
                memberTaoAccEntity.setCreatedTime(new Date());
                this.memberTaoAccService.save(memberTaoAccEntity);
            }

            for (PreLiveRoomSpecEntity preLiveRoomSpecEntity : preLiveRoomSpecEntities) {
                preLiveRoomSpecEntity.setTaobaoAccountId(taobaoAccountEntity.getAccountId());
                preLiveRoomSpecEntity.setCreatedTime(new Date());
                preLiveRoomSpecEntity.setUpdatedTime(new Date());
            }

            this.preLiveRoomSpecService.saveBatch(Arrays.asList(preLiveRoomSpecEntities));

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @PostMapping("/list")
    public R list(@RequestBody PageEntity pageEntity) {
        try {
            int pageNo = pageEntity.getPageNo();
            int pageSize = pageEntity.getPageSize();
            IPage<PreLiveRoomSpecEntity> page = this.preLiveRoomSpecService.page(new Page<>((pageNo - 1) * pageSize, pageSize));
            List<PreLiveRoomSpecEntity> list = page.getRecords();

            return R.ok()
                    .put("rooms", list)
                    .put("total_count", this.preLiveRoomSpecService..count());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @PostMapping("/recharge")
    public R recharge(@RequestParam(name = "taobao_account_id") String taobaoAccountId,
                      @RequestParam(name = "days") int days) {
        return R.error("TODO");
    }

    @PostMapping("/stop_service")
    public R stopService(@RequestParam(name = "taobao_account_id") String taobaoAccountId) {
        return R.error("TODO");
    }

    @PostMapping("/resume_service")
    public R resumeService(@RequestParam(name = "taobao_account_id") String taobaoAccountId) {
        return  R.error("TODO");
    }

    @PostMapping("/logs")
    public R logs(@RequestBody PageEntity pageEntity) {
        return R.error("TODO");
    }

    @PostMapping("/set_task")
    public R setTask(@RequestParam(name = "taobao_account_id") String taobaoAccountId,
                     @RequestParam(name = "prelives[]") LiveRoomStrategyEntity [] liveRoomStrategyEntities) {
        try {
            TaobaoAccountEntity taobaoAccountEntity = taobaoAccountService.getOne(Wrappers.<TaobaoAccountEntity>lambdaQuery().eq(TaobaoAccountEntity::getTaobaoAccountId,
                    liveRoomStrategyEntity.getTaobaoAccountId()));
            if (taobaoAccountEntity == null) {
                return R.error("找不到用户");
            }

            for (LiveRoomStrategyEntity liveRoomStrategyEntity : liveRoomStrategyEntities) {
                liveRoomStrategyEntity.setCreatedTime(new Date());
                liveRoomStrategyEntity.setUpdatedTime(new Date());
            }

            liveRoomStrategyService.saveBatch(Arrays.asList(liveRoomStrategyEntities));

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }
}
