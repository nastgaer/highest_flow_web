package highest.flow.taobaolive;

import highest.flow.taobaolive.common.defines.ScheduleState;
import highest.flow.taobaolive.common.utils.SpringContextUtils;
import highest.flow.taobaolive.job.entity.ScheduleJobEntity;
import highest.flow.taobaolive.job.service.ScheduleJobService;
import highest.flow.taobaolive.job.utils.ScheduleUtils;
import highest.flow.taobaolive.task.AutoLoginTask;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class AppRunner implements CommandLineRunner {

//    @Autowired
//    private Scheduler scheduler;
//
//    @Autowired
//    private ScheduleJobService schedulerJobService;

    @Override
    public void run(String... args) throws Exception {
        initializeJob();
    }

    private void initializeJob() {
//        try {
//            List<ScheduleJobEntity> scheduleJobList = schedulerJobService.list();
//            boolean found = false;
//            for (ScheduleJobEntity scheduleJobEntity : scheduleJobList) {
//                if (scheduleJobEntity.getBeanName().compareTo("autoLoginTask") == 0) {
//                    found = true;
//                    break;
//                }
//            }
//            if (!found) {
//                ScheduleJobEntity scheduleJobEntity = new ScheduleJobEntity();
//                scheduleJobEntity.setBeanName("autoLoginTask");
//                scheduleJobEntity.setParams(null);
//                // scheduleJobEntity.setCronExpression("0 0 0/2 1/1 * ? *");
//                scheduleJobEntity.setCronExpression("0 0/1 * * * ? *");
//                scheduleJobEntity.setCreatedTime(new Date());
//                scheduleJobEntity.setState(ScheduleState.NORMAL.getValue());
//                scheduleJobEntity.setRemark("延期任务");
//
//                // schedulerJobService.saveOrUpdate(scheduleJobEntity);
//                ScheduleUtils.createScheduleJob(scheduler, scheduleJobEntity);
//
//                scheduler.start();
//            }
//
//        } catch (SchedulerException e) {
//            e.printStackTrace();
//        }

        AutoLoginTask autoLoginTask = (AutoLoginTask) SpringContextUtils.getBean("autoLoginTask");
        autoLoginTask.run("");
    }
}
