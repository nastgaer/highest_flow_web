package highest.flow.taobaolive.taobao.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import highest.flow.taobaolive.api.param.*;
import highest.flow.taobaolive.common.annotation.SysLog;
import highest.flow.taobaolive.common.defines.ErrorCodes;
import highest.flow.taobaolive.common.utils.HFStringUtils;
import highest.flow.taobaolive.common.utils.PageUtils;
import highest.flow.taobaolive.common.utils.R;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/v1.0/ranking")
public class RankingController extends AbstractController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private TaobaoAccountService taobaoAccountService;

    @Autowired
    private TaobaoApiService taobaoApiService;

    @Autowired
    private RankingService rankingService;

    @SysLog("添加新热度任务")
    @PostMapping("/add_task")
    public R addTask(@RequestBody AddRankingTaskParam param) {
        try {
            String taocode = param.getTaocode();
            int targetScore = param.getTargetScore();
            boolean doubleBuy = param.isDoubleBuy();
            Date startTime = param.getStartTime();

            if (startTime != null && startTime.getTime() < new Date().getTime()) {
                return R.error("请正确输入开始时间");
            }

            TaobaoAccountEntity taobaoAccountEntity = this.taobaoAccountService.getActiveOne(getUser());
            if (taobaoAccountEntity == null) {
                return R.error("找不到活跃的用户");
            }

            logger.debug("找到活跃用户：" + taobaoAccountEntity.getNick());

            R r = taobaoApiService.getLiveInfo(taocode, taobaoAccountEntity);
            if (r.getCode() != ErrorCodes.SUCCESS) {
                return r;
            }

            LiveRoomEntity liveRoomEntity = (LiveRoomEntity) r.get("live_room");

            SysMember sysMember = this.getUser();

            RankingEntity rankingEntity = this.rankingService.addNewTask(sysMember,
                    taocode,
                    liveRoomEntity,
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

    @SysLog("添加新热度任务")
    @PostMapping("/add_task2")
    public R addTask2(@RequestBody AddRankingTaskParam2 param) {
        try {
            String taocode = param.getTaocode();
            String liveId = param.getLiveRoom().getLiveId();
            String accountId = param.getLiveRoom().getAccountId();
            String scopeId = param.getLiveRoom().getScopeId();
            String subScopeId = param.getLiveRoom().getSubScopeId();

            int targetScore = param.getTargetScore();
            boolean doubleBuy = param.isDoubleBuy();
            Date startTime = param.getStartTime();

            LiveRoomEntity liveRoomEntity = new LiveRoomEntity();

            liveRoomEntity.setLiveId(liveId);
            liveRoomEntity.setAccountId(accountId);
            liveRoomEntity.getHierarchyData().setScopeId(scopeId);
            liveRoomEntity.getHierarchyData().setSubScopeId(subScopeId);

            SysMember sysMember = this.getUser();

            RankingEntity rankingEntity = this.rankingService.addNewTask(sysMember,
                    taocode,
                    liveRoomEntity,
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

    //@SysLog("获取当天的任务")
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

    @SysLog("获取任务执行情况")
    @PostMapping("/get")
    public R get(@RequestBody Map<String, Object> param) {
        try {

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @SysLog("开始任务")
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
                return R.error("找不到等待任务");
            }

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @SysLog("停止任务")
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

    @SysLog("获取刷记录")
    @PostMapping("/logs")
    public R logs(@RequestBody PageParam pageParam) {
        try {
            SysMember sysMember = this.getUser();
            PageUtils pageUtils = this.rankingService.queryPage(sysMember, pageParam);

            return R.ok().put("logs", pageUtils.getList()).put("total_count", pageUtils.getTotalCount());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }
}

