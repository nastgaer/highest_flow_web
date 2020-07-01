package highest.flow.taobaolive.taobao.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import highest.flow.taobaolive.common.defines.ErrorCodes;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.job.defines.ScheduleState;
import highest.flow.taobaolive.job.entity.ScheduleJobEntity;
import highest.flow.taobaolive.job.service.ScheduleJobService;
import highest.flow.taobaolive.job.utils.ScheduleUtils;
import highest.flow.taobaolive.sys.entity.PageEntity;
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
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private ScheduleJobService schedulerJobService;

    @PostMapping("/parse_taocode")
    public R parseTaoCode(@RequestParam(name = "taocode") String taocode) {
        try {
            R r = taobaoApiService.parseTaoCode(taocode);
            if (r.getCode() != ErrorCodes.SUCCESS) {
                return r;
            }

            LiveRoomEntity liveRoomEntity = new LiveRoomEntity();
            liveRoomEntity.setLiveId((String) r.get("liveId"));
            liveRoomEntity.setCreatorId((String) r.get("creatorId"));
            liveRoomEntity.setTalentLiveUrl((String) r.get("talentLiveUrl"));

            String liveId = (String) r.get("liveId");
            r = taobaoApiService.getLiveDetail(liveId);
            if (r.getCode() != ErrorCodes.SUCCESS) {
                return r;
            }

            liveRoomEntity.setAccountId((String) r.get("accountId"));
            liveRoomEntity.setAccountName((String) r.get("accountName"));
            liveRoomEntity.setFansNum((int) r.get("fansNum"));
            liveRoomEntity.setTopic((String) r.get("topic"));
            liveRoomEntity.setViewCount((int) r.get("viewCount"));
            liveRoomEntity.setPraiseCount((int) r.get("praiseCount"));
            liveRoomEntity.setOnlineCount((int) r.get("onlineCount"));
            liveRoomEntity.setCoverImg((String) r.get("coverImg"));
            liveRoomEntity.setCoverImg169((String) r.get("coverImg169"));
            liveRoomEntity.setTitle((String) r.get("title"));
            liveRoomEntity.setIntro((String) r.get("intro"));
            liveRoomEntity.setChannelId((int) r.get("channelId"));
            liveRoomEntity.setColumnId((int) r.get("columnId"));
            liveRoomEntity.setLocation((String) r.get("location"));

            return R.ok()
                    .put("liveRoomEntity", liveRoomEntity);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error("解析淘口令失败");
    }

    @PostMapping("/add_task")
    public R addTask(@RequestParam(name = "taocode") String taocode,
                     @RequestParam(name = "target_score") int targetScore,
                     @RequestParam(name = "double_buy") boolean doubleBuy,
                     @RequestParam(name = "start_time") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime) {
        try {
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

            LiveRoomEntity liveRoomEntity = (LiveRoomEntity) r.get("liveRoomEntity");

            SysMember sysMember = this.getUser();

            RankingEntity rankingEntity = new RankingEntity();
            rankingEntity.setMemberId(sysMember.getId());
            rankingEntity.setTaocode(taocode);
            rankingEntity.setLiveId(liveRoomEntity.getLiveId());
            rankingEntity.setRoomName(liveRoomEntity.getAccountName());
            rankingEntity.setStartScore(liveRoomEntity.getRankingScore());
            rankingEntity.setTargetScore(targetScore);
            rankingEntity.setDoubleBuy(doubleBuy);
            rankingEntity.setStartTime(startTime);
            rankingEntity.setEndTime(null);
            rankingEntity.setState(RankingEntityState.Waiting.getState());
            rankingEntity.setMsg(r.getMsg());
            rankingEntity.setCreatedTime(new Date());
            rankingEntity.setUpdatedTime(new Date());
            rankingService.save(rankingEntity);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startTime);
            int year = calendar.get(Calendar.YEAR) + 1900;
            int month = calendar.get(Calendar.MONTH);
            int date = calendar.get(Calendar.DAY_OF_MONTH);
            int hour = calendar.get(Calendar.HOUR);
            int minute = calendar.get(Calendar.MINUTE);
            int second = calendar.get(Calendar.SECOND);

            String expression = String.format("%d %d %d %d %d ? %d", second, minute, hour, date, month, year);

            ScheduleJobEntity scheduleJobEntity = new ScheduleJobEntity();
            scheduleJobEntity.setBeanName("assitRankingTask");
            scheduleJobEntity.setParams(String.valueOf(rankingEntity.getId()));
            scheduleJobEntity.setCronExpression(expression);
            scheduleJobEntity.setCreatedTime(new Date());
            scheduleJobEntity.setState(ScheduleState.NORMAL.getValue());
            scheduleJobEntity.setRemark("延期任务");

            schedulerJobService.saveOrUpdate(scheduleJobEntity);
            ScheduleUtils.createScheduleJob(scheduler, scheduleJobEntity);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error("添加助力任务失败");
    }

    @PostMapping("/todays")
    public R todays(@RequestParam(name = "date") @DateTimeFormat(pattern = "yyyy-MM-dd") Date currentDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String date = sdf.format(currentDate);
            List<RankingEntity> rankingEntities = this.rankingService.list(Wrappers.<RankingEntity>lambdaQuery().likeRight("created_time", date));

            return R.ok().put("ranking", rankingEntities);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @PostMapping("/start_task")
    public R startTask(@RequestParam(name = "task_id") int taskId) {
        try {
            RankingEntity rankingEntity = this.rankingService.getById(taskId);
            if (rankingEntity == null) {
                return R.error("找不到任务");
            }

            if (rankingEntity.getState() == RankingEntityState.Running) {
                return R.error("已经开始了");
            }
            if (rankingEntity.getState() == RankingEntityState.Error) {
                return R.error("有错误");
            }
            if (rankingEntity.getState() == RankingEntityState.Finished) {
                return R.error("已经结束了");
            }

            ScheduleJobEntity scheduleJobEntity = this.rankingService.list(Wrappers.<ScheduleJobEntity>lambdaQuery()
                    .eq(ScheduleJobEntity::getParams, String.valueOf(taskId)));
            if (scheduleJobEntity == null) {
                return R.error("找不到JOB");
            }

            ScheduleUtils.run(scheduler, scheduleJobEntity);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @PostMapping("/stop_task")
    public R stopTask(@RequestParam(name = "task_id") int taskId) {
        // TODO
        return R.error("TODO");
    }

    @PostMapping("/logs")
    public R logs(@RequestBody PageEntity pageEntity) {
        // TODO
        return R.error("TODO");
    }
}

