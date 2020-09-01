package highest.flow.taobaolive.job.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import highest.flow.taobaolive.job.defines.ScheduleState;
import highest.flow.taobaolive.job.dao.ScheduleJobDao;
import highest.flow.taobaolive.job.entity.ScheduleJobEntity;
import highest.flow.taobaolive.job.service.ScheduleJobService;
import highest.flow.taobaolive.job.utils.ScheduleUtils;
import org.quartz.CronTrigger;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
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
            String beanName = scheduleJob.getBeanName();
            if (beanName.compareTo("autoLoginTask") == 0) {
                continue;
            }

            CronTrigger cronTrigger = ScheduleUtils.getCronTrigger(scheduler, scheduleJob.getId());

            try {
                // 如果不存在，则创建
                if (cronTrigger == null) {
                    ScheduleUtils.createScheduleJob(scheduler, scheduleJob);

                } else {
                    ScheduleUtils.updateScheduleJob(scheduler, scheduleJob);
                }

            } catch (Exception ex) {
                ex.printStackTrace();
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
    public void updateJob(ScheduleJobEntity scheduleJob) {
        ScheduleUtils.updateScheduleJob(scheduler, scheduleJob);

        this.updateById(scheduleJob);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void runJob(ScheduleJobEntity scheduleJob) {
        scheduleJob.setState(ScheduleState.NORMAL.getValue());
        this.updateById(scheduleJob);

        ScheduleUtils.run(scheduler, scheduleJob);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void stopJob(ScheduleJobEntity scheduleJob) {
        scheduleJob.setState(ScheduleState.PAUSE.getValue());
        this.updateById(scheduleJob);

        ScheduleUtils.pauseJob(scheduler, scheduleJob.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteJob(ScheduleJobEntity scheduleJob) {
        scheduleJob.setState(ScheduleState.PAUSE.getValue());
        this.removeById(scheduleJob);

        ScheduleUtils.deleteScheduleJob(scheduler, scheduleJob.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resumeJob(ScheduleJobEntity scheduleJob) {
        scheduleJob.setState(ScheduleState.NORMAL.getValue());
        this.updateById(scheduleJob);

        ScheduleUtils.resumeJob(scheduler, scheduleJob.getId());
    }

    @Override
    public ScheduleJobEntity findRunnigJob(String beanName, String param) {
        try {
            ScheduleJobEntity scheduleJobEntity = null;

            List<JobExecutionContext> jobExecutionContexts = scheduler.getCurrentlyExecutingJobs();
            for (JobExecutionContext jobExecutionContext : jobExecutionContexts) {
                ScheduleJobEntity runningJobEntity = (ScheduleJobEntity)jobExecutionContext.getMergedJobDataMap().get(ScheduleJobEntity.JOB_PARAM_KEY);
                try {
                    if (runningJobEntity.getBeanName().compareTo(beanName) != 0)
                        continue;

                    if (String.valueOf(runningJobEntity.getParams()).compareTo(param) == 0) {
                        return runningJobEntity;
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public ScheduleJobEntity findScheduledJob(String beanName, String param) {
        return this.getOne(Wrappers.<ScheduleJobEntity>lambdaQuery()
                .eq(ScheduleJobEntity::getBeanName, beanName)
                .eq(ScheduleJobEntity::getParams, param));
    }
}
