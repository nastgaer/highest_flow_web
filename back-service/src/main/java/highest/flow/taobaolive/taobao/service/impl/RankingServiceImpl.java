package highest.flow.taobaolive.taobao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import highest.flow.taobaolive.api.param.PageParam;
import highest.flow.taobaolive.common.utils.HFStringUtils;
import highest.flow.taobaolive.common.utils.PageUtils;
import highest.flow.taobaolive.common.utils.Query;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.job.defines.ScheduleState;
import highest.flow.taobaolive.job.entity.ScheduleJobEntity;
import highest.flow.taobaolive.job.service.ScheduleJobService;
import highest.flow.taobaolive.job.utils.ScheduleUtils;
import highest.flow.taobaolive.sys.entity.SysMember;
import highest.flow.taobaolive.taobao.dao.RankingTaskDao;
import highest.flow.taobaolive.taobao.defines.RankingEntityState;
import highest.flow.taobaolive.taobao.entity.LiveRoomEntity;
import highest.flow.taobaolive.taobao.entity.RankingEntity;
import highest.flow.taobaolive.taobao.entity.TaobaoAccountEntity;
import highest.flow.taobaolive.taobao.service.RankingService;
import highest.flow.taobaolive.taobao.service.TaobaoAccountService;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

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
    private CacheManager cacheManager;

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
    public RankingEntity addNewTask(SysMember sysMember, String taocode, LiveRoomEntity liveRoomEntity, int targetScore, boolean doubleBuy, Date startTime) {
        try {
            RankingEntity rankingEntity = new RankingEntity();
            rankingEntity.setMemberId(sysMember.getId());
            rankingEntity.setTaocode(taocode);
            rankingEntity.setLiveId(liveRoomEntity.getLiveId());
            rankingEntity.setLiveAccountId(liveRoomEntity.getAccountId());
            rankingEntity.setLiveScopeId(liveRoomEntity.getHierarchyData().getScopeId());
            rankingEntity.setLiveSubScopeId(liveRoomEntity.getHierarchyData().getSubScopeId());
            rankingEntity.setRoomName(liveRoomEntity.getAccountName());
            rankingEntity.setStartScore(liveRoomEntity.getRankingListData().getRankingScore());
            rankingEntity.setTargetScore(targetScore);
            rankingEntity.setDoubleBuy(doubleBuy);
            rankingEntity.setStartTime(startTime == null ? new Date() : startTime);
            rankingEntity.setEndTime(null);
            rankingEntity.setState(RankingEntityState.Waiting.getState());
            rankingEntity.setMsg("");
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
            }

            ScheduleJobEntity scheduleJobEntity = new ScheduleJobEntity();
            scheduleJobEntity.setBeanName("assistRankingTask");
            scheduleJobEntity.setParams(String.valueOf(rankingEntity.getId()));
            scheduleJobEntity.setCronExpression(expression);
            scheduleJobEntity.setCreatedTime(new Date());
            scheduleJobEntity.setState(ScheduleState.NORMAL.getValue());
            scheduleJobEntity.setRemark("刷热度");

            if (startTime == null) { // 立即执行
                ScheduleUtils.runInstant(scheduler, scheduleJobEntity);

            } else {
                this.schedulerJobService.saveJob(scheduleJobEntity);

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

            this.schedulerJobService.runJob(scheduleJobEntity);

            ScheduleUtils.run(scheduler, scheduleJobEntity);

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
                return false;
            }

            if (scheduleJobEntity == null) {
                return false;
            }

            this.schedulerJobService.stopJob(scheduleJobEntity);

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
            if (scheduleJobEntity == null) {
                return false;
            }

            ScheduleUtils.deleteScheduleJob(scheduler, scheduleJobEntity.getId());
            this.schedulerJobService.removeById(scheduleJobEntity);

            return true;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    @Cacheable(value = "assistRankAccounts", key = "#sysMember.id + '_' + #liveId")
    public List<TaobaoAccountEntity> availableAccounts(SysMember sysMember, String liveId) {
        try {
            List<TaobaoAccountEntity> taobaoAccountEntities = taobaoAccountService.getActiveAllByMember(sysMember);

            Cache cache = cacheManager.getCache("assistRankUids");
            String key = String.valueOf(sysMember.getId()) + "_" + liveId;
            Cache.ValueWrapper valueWrapper = cache.get(key);
            List<String> value = valueWrapper == null ? new ArrayList<>() : (List<String>) valueWrapper.get();

            List<TaobaoAccountEntity> availableAccounts = new ArrayList<>();
            availableAccounts.addAll(taobaoAccountEntities);

            for (String uid : value) {
                availableAccounts.removeIf(acc -> acc.getUid().compareTo(uid) == 0);
            }

            return availableAccounts;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    @CachePut(value = "assistRankAccounts", key = "#sysMember.id + '_' + #liveId", unless = "#result == null")
    public List<TaobaoAccountEntity> markAssist(SysMember sysMember, String liveId, List<TaobaoAccountEntity> markedAccounts) {
        try {
            // 先标记已经助力完的账号
            Cache cache = cacheManager.getCache("assistRankUids");
            String key = String.valueOf(sysMember.getId()) + "_" + liveId;
            Cache.ValueWrapper valueWrapper = cache.get(key);
            List<String> value = valueWrapper == null ? new ArrayList<>() : (List<String>) valueWrapper.get();

            for (TaobaoAccountEntity markedAccount : markedAccounts) {
                value.add(markedAccount.getUid());
            }

            cache.put(key, value);

            // 重新获取尚未助力的小号列表
            List<TaobaoAccountEntity> taobaoAccountEntities = taobaoAccountService.getActiveAllByMember(sysMember);

            List<TaobaoAccountEntity> availableAccounts = new ArrayList<>();
            availableAccounts.addAll(taobaoAccountEntities);

            for (String uid : value) {
                availableAccounts.removeIf(acc -> acc.getUid().compareTo(uid) == 0);
            }

            return availableAccounts;

        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return null;
    }
}
