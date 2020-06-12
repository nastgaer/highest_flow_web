package highest.flow.taobaolive.job.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import highest.flow.taobaolive.common.defines.ScheduleState;
import highest.flow.taobaolive.job.dao.ScheduleJobDao;
import highest.flow.taobaolive.job.entity.ScheduleJobEntity;
import highest.flow.taobaolive.job.service.ScheduleJobService;
import highest.flow.taobaolive.job.utils.ScheduleUtils;
import org.quartz.CronTrigger;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.*;

@Service("scheduleJobService")
public class ScheduleJobServiceImpl extends ServiceImpl<ScheduleJobDao, ScheduleJobEntity> implements ScheduleJobService {

    @Autowired
    private Scheduler scheduler;

    @PostConstruct
    public void init() {

        List<ScheduleJobEntity> scheduleJobList = this.list();
        for (ScheduleJobEntity scheduleJob : scheduleJobList) {
            CronTrigger cronTrigger = ScheduleUtils.getCronTrigger(scheduler, scheduleJob.getId());

            // 如果不存在，则创建
            if (cronTrigger == null) {
                ScheduleUtils.createScheduleJob(scheduler, scheduleJob);
            } else {
                ScheduleUtils.updateScheduleJob(scheduler, scheduleJob);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveJob(ScheduleJobEntity scheduleJob) {
        scheduleJob.setCreatedTime(new Date());
        scheduleJob.setState(ScheduleState.NORMAL.getValue());
        this.save(scheduleJob);

        ScheduleUtils.createScheduleJob(scheduler, scheduleJob);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ScheduleJobEntity scheduleJob) {
        ScheduleUtils.updateScheduleJob(scheduler, scheduleJob);

        this.updateById(scheduleJob);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(Long[] jobIds) {
        for (Long jobId : jobIds) {
            ScheduleUtils.deleteScheduleJob(scheduler, jobId);
        }

        // 删除数据
        this.removeByIds(Arrays.asList(jobIds));
    }

    @Override
    public int updateBatch(Long[] jobIds, int state) {
        Map<String, Object> map = new HashMap<>(2);
        map.put("list", jobIds);
        map.put("state", state);
        return baseMapper.updateBatch(map);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void run(Long[] jobIds) {
        for (Long jobId : jobIds) {
            ScheduleUtils.run(scheduler, this.getById(jobId));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pause(Long[] jobIds) {
        for (Long jobId : jobIds) {
            ScheduleUtils.pauseJob(scheduler, jobId);
        }

        updateBatch(jobIds, ScheduleState.PAUSE.getValue());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resume(Long[] jobIds) {
        for (Long jobId : jobIds) {
            ScheduleUtils.resumeJob(scheduler, jobId);
        }

        updateBatch(jobIds, ScheduleState.NORMAL.getValue());
    }


}
