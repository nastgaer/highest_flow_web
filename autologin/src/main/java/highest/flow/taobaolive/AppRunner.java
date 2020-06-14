package highest.flow.taobaolive;

import highest.flow.taobaolive.common.defines.ScheduleState;
import highest.flow.taobaolive.job.entity.ScheduleJobEntity;
import highest.flow.taobaolive.job.service.ScheduleJobService;
import highest.flow.taobaolive.job.utils.ScheduleUtils;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class AppRunner implements CommandLineRunner {

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private ScheduleJobService schedulerJobService;

    @Override
    public void run(String... args) throws Exception {
        initializeJob();
    }

    private void initializeJob() {
        try {
            List<ScheduleJobEntity> scheduleJobList = schedulerJobService.list();
            if (scheduleJobList.size() < 1) {
                ScheduleJobEntity scheduleJobEntity = new ScheduleJobEntity();
                scheduleJobEntity.setBeanName("highest.flow.taobaolive.task.autoLoginTask");
                scheduleJobEntity.setParams(null);
                scheduleJobEntity.setCronExpression("0 0 0/2 1/1 * ? *");
                scheduleJobEntity.setCreatedTime(new Date());
                scheduleJobEntity.setState(ScheduleState.NORMAL.getValue());
                scheduleJobEntity.setRemark("延期任务");

                schedulerJobService.saveOrUpdate(scheduleJobEntity);
                ScheduleUtils.createScheduleJob(scheduler, scheduleJobEntity);

                scheduler.start();
            }

        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }
}
