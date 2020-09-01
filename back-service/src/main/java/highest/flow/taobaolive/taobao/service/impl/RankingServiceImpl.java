package highest.flow.taobaolive.taobao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import highest.flow.taobaolive.api.param.PageParam;
import highest.flow.taobaolive.common.utils.*;
import highest.flow.taobaolive.job.defines.ScheduleState;
import highest.flow.taobaolive.job.entity.ScheduleJobEntity;
import highest.flow.taobaolive.job.service.ScheduleJobService;
import highest.flow.taobaolive.job.utils.ScheduleUtils;
import highest.flow.taobaolive.sys.entity.SysMember;
import highest.flow.taobaolive.sys.service.SysConfigService;
import highest.flow.taobaolive.taobao.dao.RankingTaskDao;
import highest.flow.taobaolive.taobao.defines.RankingEntityState;
import highest.flow.taobaolive.taobao.defines.RankingScore;
import highest.flow.taobaolive.taobao.entity.LiveRoomEntity;
import highest.flow.taobaolive.taobao.entity.RankingEntity;
import highest.flow.taobaolive.taobao.entity.TaobaoAccountEntity;
import highest.flow.taobaolive.taobao.provider.LiveCacheManager;
import highest.flow.taobaolive.taobao.service.RankingService;
import highest.flow.taobaolive.taobao.service.TaobaoAccountService;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service("rankingService")
public class RankingServiceImpl extends ServiceImpl<RankingTaskDao, RankingEntity> implements RankingService {

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private ScheduleJobService schedulerJobService;

    @Autowired
    private TaobaoAccountService taobaoAccountService;

    @Autowired
    private LiveCacheManager cacheManager;

    @Autowired
    private SysConfigService sysConfigService;

    private Map<RankingScore, Integer> rankingScoreMap = new HashMap<>();

    @PostConstruct
    public void init() {
        for (RankingScore rankingScore : RankingScore.values()) {
            String value = sysConfigService.getValue(rankingScore.getConfKey());
            int score = NumberUtils.valueOf(NumberUtils.parseInt(value));

            rankingScoreMap.put(rankingScore, score);
        }

        // 因为强制关闭，把所状态执行中状态的任务标记为错误
        List<RankingEntity> runningEntities = this.baseMapper.selectList(Wrappers.<RankingEntity>lambdaQuery()
                .eq(RankingEntity::getState, RankingEntityState.Running.getState()));
        if (runningEntities != null) {
            for (RankingEntity rankingEntity : runningEntities) {
                this.errorTask(rankingEntity);
            }
        }
    }

    @Override
    public int getRankingUnitScore(RankingScore rankingScore) {
        Integer value = rankingScoreMap.get(rankingScore);
        return value == null ? 0 : value.intValue();
    }

    @Override
    public PageUtils queryPage(SysMember sysMember, PageParam pageParam) {
        int pageNo = pageParam.getPageNo();
        int pageSize = pageParam.getPageSize();
        String keyword = pageParam.getKeyword();

        int memberId = sysMember == null || sysMember.isAdministrator() ? 0 : sysMember.getId();

        Map<String, Object> params = new HashMap<>();
        params.put(Query.PAGE, pageNo);
        params.put(Query.LIMIT, pageSize);

        QueryWrapper<RankingEntity> queryWrapper = new QueryWrapper<>();
        if (!HFStringUtils.isNullOrEmpty(keyword)) {
            queryWrapper.like("room_name", keyword);
        }
        if (memberId > 0) {
            queryWrapper.like("member_id", memberId);
        }

        IPage<RankingEntity> page = this.page(new Query<RankingEntity>().getPage(params), queryWrapper);
        return new PageUtils<RankingEntity>(page);
    }

    @Override
    public List<RankingEntity> getTodaysTask(String today) {
        return this.baseMapper.queryTodaysTask(today);
    }

    @Override
    public RankingEntity getRunningTask(SysMember sysMember, String liveId) {
        try {
            QueryWrapper<RankingEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper
                    .and(i -> i.eq("member_id", sysMember.getId())
                                .eq("live_id", liveId))
                    .and(i -> i.eq("state", RankingEntityState.Running.getState())
                            .or()
                            .eq("state", RankingEntityState.Waiting.getState()));

            return this.baseMapper.selectOne(queryWrapper);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public RankingEntity addNewTask(SysMember sysMember,
                                    String taocode,
                                    String liveId, String accountName,
                                    int targetScore,
                                    boolean hasFollow, boolean hasStay, boolean hasBuy,
                                    boolean hasDoubleBuy, Date startTime, String comment) {
        try {
            RankingEntity rankingEntity = new RankingEntity();
            rankingEntity.setMemberId(sysMember.getId());
            rankingEntity.setTaocode(taocode);
            rankingEntity.setLiveId(liveId);
            rankingEntity.setRoomName(accountName);
            rankingEntity.setStartScore(0);
            rankingEntity.setEndScore(0);
            rankingEntity.setTargetScore(targetScore);
            rankingEntity.setHasFollow(hasFollow);
            rankingEntity.setHasStay(hasStay);
            rankingEntity.setHasBuy(hasBuy);
            rankingEntity.setHasDoubleBuy(hasDoubleBuy);
            rankingEntity.setStartTime(startTime == null ? new Date() : startTime);
            rankingEntity.setEndTime(null);
            rankingEntity.setComment(comment);
            rankingEntity.setMsg("");
            rankingEntity.setState(RankingEntityState.Waiting.getState());
            rankingEntity.setCreatedTime(new Date());
            rankingEntity.setUpdatedTime(new Date());

            this.save(rankingEntity);

            String expression = "";

            if (startTime != null) { // 指定时间
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(startTime);
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH) + 1;
                int date = calendar.get(Calendar.DAY_OF_MONTH);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                int second = calendar.get(Calendar.SECOND);

                expression = String.format("%d %d %d %d %d ? %d", second, minute, hour, date, month, year);

            } else {
                Date next = CommonUtils.addMinutes(new Date(), 5);

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(next);
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH) + 1;
                int date = calendar.get(Calendar.DAY_OF_MONTH);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                int second = calendar.get(Calendar.SECOND);

                expression = String.format("%d %d %d %d %d ? %d", second, minute, hour, date, month, year);
            }

            ScheduleJobEntity scheduleJobEntity = new ScheduleJobEntity();
            scheduleJobEntity.setBeanName("assistRankingTask");
            scheduleJobEntity.setParams(String.valueOf(rankingEntity.getId()));
            scheduleJobEntity.setCronExpression(expression);
            scheduleJobEntity.setCreatedTime(new Date());
            scheduleJobEntity.setState(ScheduleState.NORMAL.getValue());
            scheduleJobEntity.setRemark("刷热度");

            this.schedulerJobService.saveJob(scheduleJobEntity);

            if (startTime == null) { // 立即开始
                this.startTask(rankingEntity);
            }

            return rankingEntity;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean startTask(RankingEntity rankingEntity) {
        try {
            int taskId = rankingEntity.getId();
            ScheduleJobEntity scheduleJobEntity = this.schedulerJobService.findScheduledJob("assistRankingTask", String.valueOf(taskId));

            if (scheduleJobEntity == null) {
                return false;
            }

            // 立即开始任务
            this.schedulerJobService.runJob(scheduleJobEntity);
            // 避免重现开始定时任务，把已经设置的定时任务删掉
            this.schedulerJobService.deleteJob(scheduleJobEntity);

            return true;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean stopTask(RankingEntity rankingEntity) {
        try {
            int taskId = rankingEntity.getId();
            ScheduleJobEntity scheduleJobEntity = this.schedulerJobService.findRunnigJob("assistRankingTask", String.valueOf(taskId));

            if (scheduleJobEntity == null) {
                scheduleJobEntity = this.schedulerJobService.findScheduledJob("assistRankingTask", String.valueOf(taskId));
            }

            if (scheduleJobEntity == null) {
                return false;
            }

            this.schedulerJobService.stopJob(scheduleJobEntity);
            // 通过把任务状态为停止，所有有关的线程自然会停止

            return true;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteTask(RankingEntity rankingEntity) {
        try {
            int taskId = rankingEntity.getId();
            ScheduleJobEntity scheduleJobEntity = this.schedulerJobService.findRunnigJob("assistRankingTask", String.valueOf(taskId));
            if (scheduleJobEntity != null) {
                return false;
            }

            scheduleJobEntity = this.schedulerJobService.findScheduledJob("assistRankingTask", String.valueOf(taskId));
            if (scheduleJobEntity != null) {
                this.schedulerJobService.deleteJob(scheduleJobEntity);
            }

            this.removeById(rankingEntity);

            return true;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean errorTask(RankingEntity rankingEntity) {
        try {
            // 先强制关闭任务
            int taskId = rankingEntity.getId();
            ScheduleJobEntity scheduleJobEntity = this.schedulerJobService.findRunnigJob("assistRankingTask", String.valueOf(taskId));
            if (scheduleJobEntity == null) {
                scheduleJobEntity = this.schedulerJobService.findScheduledJob("assistRankingTask", String.valueOf(taskId));
            }

            if (scheduleJobEntity != null) {
                this.schedulerJobService.stopJob(scheduleJobEntity);
                this.schedulerJobService.deleteJob(scheduleJobEntity);
            }

            // 标记为错误
            rankingEntity.setState(RankingEntityState.Error.getState());
            rankingEntity.setEndTime(new Date());
            rankingEntity.setUpdatedTime(new Date());
            this.updateById(rankingEntity);

            return true;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public List<TaobaoAccountEntity> availableAccounts(SysMember sysMember, String liveId) {
        try {
            // 采集该会员所属于的所有的小号列表
            List<TaobaoAccountEntity> taobaoAccountEntities = taobaoAccountService.getActiveAllByMember(sysMember);

            // 查看有没有今天刷过的小号列表
            List<String> uids = cacheManager.getCachedRankingAccounts(sysMember, liveId, new Date());

            // 筛选今天刷过的小号列表
            List<TaobaoAccountEntity> availableAccounts = new ArrayList<>();
            for (TaobaoAccountEntity taobaoAccountEntity : taobaoAccountEntities) {
                if (uids != null) {
                    String uid = taobaoAccountEntity.getUid();
                    if (uids.indexOf(uid) >= 0) {
                        continue;
                    }
                }
                availableAccounts.add(taobaoAccountEntity);
            }

            return availableAccounts;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public void markAssist(SysMember sysMember, String liveId, List<TaobaoAccountEntity> markedAccounts) {
        try {
            List<String> uids = cacheManager.getCachedRankingAccounts(sysMember, liveId, new Date());

            if (uids == null) {
                uids = new ArrayList<>();
            }

            for (TaobaoAccountEntity markedAccount : markedAccounts) {
                uids.add(markedAccount.getUid());
            }

            cacheManager.cacheRankingAccounts(sysMember, liveId, new Date(), uids);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
