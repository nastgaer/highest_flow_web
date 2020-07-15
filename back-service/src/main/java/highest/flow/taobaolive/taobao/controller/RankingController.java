package highest.flow.taobaolive.taobao.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import highest.flow.taobaolive.api.param.AddRankingTaskParam;
import highest.flow.taobaolive.api.param.ControlRankingTaskParam;
import highest.flow.taobaolive.api.param.TodayRankingParam;
import highest.flow.taobaolive.common.defines.ErrorCodes;
import highest.flow.taobaolive.common.utils.HFStringUtils;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.api.param.PageParam;
import highest.flow.taobaolive.sys.controller.AbstractController;
import highest.flow.taobaolive.sys.entity.SysMember;
import highest.flow.taobaolive.taobao.defines.RankingEntityState;
import highest.flow.taobaolive.taobao.defines.TaobaoAccountState;
import highest.flow.taobaolive.taobao.entity.LiveRoomEntity;
import highest.flow.taobaolive.taobao.entity.RankingEntity;
import highest.flow.taobaolive.taobao.entity.TaobaoAccountEntity;
import highest.flow.taobaolive.taobao.service.RankingService;
import highest.flow.taobaolive.taobao.service.TaobaoAccountService;
import highest.flow.taobaolive.taobao.service.TaobaoApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/ranking")
public class RankingController extends AbstractController {

    @Autowired
    private TaobaoAccountService taobaoAccountService;

    @Autowired
    private TaobaoApiService taobaoApiService;

    @Autowired
    private RankingService rankingService;

    @PostMapping("/add_task")
    public R addTask(@RequestBody AddRankingTaskParam param) {
        try {
            String taocode = param.getTaocode();
            int targetScore = param.getTargetScore();
            boolean doubleBuy = param.isDoubleBuy();
            Date startTime = param.getStartTime();

            if (startTime.getTime() < new Date().getTime()) {
                return R.error("请正确输入开始时间");
            }

            TaobaoAccountEntity taobaoAccountEntity = this.taobaoAccountService.getOne(Wrappers.<TaobaoAccountEntity>lambdaQuery().eq(TaobaoAccountEntity::getState, TaobaoAccountState.Normal.getState()));
            if (taobaoAccountEntity == null) {
                return R.error("找不到活跃的用户");
            }

            R r = taobaoApiService.getLiveInfo(taocode, taobaoAccountEntity);
            if (r.getCode() != ErrorCodes.SUCCESS) {
                return r;
            }

            LiveRoomEntity liveRoomEntity = (LiveRoomEntity) r.get("live_room");

            SysMember sysMember = this.getUser();

            RankingEntity rankingEntity = this.rankingService.addNewTask(sysMember,
                    liveRoomEntity,
                    taocode,
                    targetScore,
                    doubleBuy,
                    startTime);

            if (rankingEntity == null) {
                return R.error();
            }

            return R.ok().put("task_id", rankingEntity.getId());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error("添加助力任务失败");
    }

    @PostMapping("/todays")
    public R todays(@RequestBody TodayRankingParam param) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String date = sdf.format(param.getCurrentDate());
            List<RankingEntity> rankingEntities = this.rankingService.getTodaysTask(date);

            return R.ok().put("ranking", rankingEntities);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @PostMapping("/start_task")
    public R startTask(@RequestBody ControlRankingTaskParam param) {
        try {
            int taskId = param.getTaskId();

            RankingEntity rankingEntity = this.rankingService.getById(taskId);
            if (rankingEntity == null) {
                return R.error("找不到任务");
            }

            if (rankingEntity.getState() == RankingEntityState.Running.getState()) {
                return R.error("已经开始了");
            }
            if (rankingEntity.getState() == RankingEntityState.Error.getState()) {
                return R.error("有错误");
            }
            if (rankingEntity.getState() == RankingEntityState.Finished.getState()) {
                return R.error("已经结束了");
            }

            if (!this.rankingService.startTask(rankingEntity)) {
                return R.error("开始任务失败");
            }

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @PostMapping("/stop_task")
    public R stopTask(@RequestBody ControlRankingTaskParam param) {
        try {
            int taskId = param.getTaskId();

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

    @PostMapping("/logs")
    public R logs(@RequestBody PageParam pageParam) {
        try {
            int pageNo = pageParam.getPageNo();
            int pageSize = pageParam.getPageSize();
            String keyword = pageParam.getKeyword();

            IPage<RankingEntity> page =
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
        return R.error("TODO");
    }
}

