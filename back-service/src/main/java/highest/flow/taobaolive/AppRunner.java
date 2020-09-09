package highest.flow.taobaolive;

import highest.flow.taobaolive.common.utils.SpringContextUtils;
import highest.flow.taobaolive.job.defines.ScheduleState;
import highest.flow.taobaolive.job.entity.ScheduleJobEntity;
import highest.flow.taobaolive.job.service.ScheduleJobService;
import highest.flow.taobaolive.job.utils.ScheduleUtils;
import highest.flow.taobaolive.taobao.provider.TaobaoAccountProvider;
import highest.flow.taobaolive.taobao.service.TaobaoAccountService;
import highest.flow.taobaolive.task.AutoLoginTask;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class AppRunner implements CommandLineRunner {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private ScheduleJobService schedulerJobService;

    @Value("${autologin.master.init-start:false}")
    private boolean initStart;

    @Autowired
    private TaobaoAccountProvider taobaoAccountProvider;

    @Override
    public void run(String... args) throws Exception {
        logger.info("初始化开始了");

        initializeAutoLoginJob();

        initializeMemberServiceJob();

        initializeCache();

        logger.info("初始化结束了");
    }

    private void initializeMemberServiceJob() {
        try {
            List<ScheduleJobEntity> scheduleJobList = schedulerJobService.list();
            boolean found = false;
            for (ScheduleJobEntity scheduleJobEntity : scheduleJobList) {
                if (scheduleJobEntity.getBeanName().compareTo("memberServiceTask") == 0) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                ScheduleJobEntity scheduleJobEntity = new ScheduleJobEntity();
                scheduleJobEntity.setBeanName("memberServiceTask");
                scheduleJobEntity.setParams(null);
                scheduleJobEntity.setCronExpression("0 30 2 ? * * *");
                scheduleJobEntity.setCreatedTime(new Date());
                scheduleJobEntity.setState(ScheduleState.NORMAL.getValue());
                scheduleJobEntity.setRemark("会员服务任务");

                schedulerJobService.saveOrUpdate(scheduleJobEntity);
                ScheduleUtils.createScheduleJob(scheduler, scheduleJobEntity);

                scheduler.start();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void initializeAutoLoginJob() {
        try {
            List<ScheduleJobEntity> scheduleJobList = schedulerJobService.list();
            boolean found = false;
            for (ScheduleJobEntity scheduleJobEntity : scheduleJobList) {
                if (scheduleJobEntity.getBeanName().compareTo("autoLoginTask") == 0) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                ScheduleJobEntity scheduleJobEntity = new ScheduleJobEntity();
                scheduleJobEntity.setBeanName("autoLoginTask");
                scheduleJobEntity.setParams(null);
                // EVERY 4 hours begining at 0:00
                scheduleJobEntity.setCronExpression("0 30 0/8 ? * * *");
                scheduleJobEntity.setCreatedTime(new Date());
                scheduleJobEntity.setState(ScheduleState.NORMAL.getValue());
                scheduleJobEntity.setRemark("延期任务");

                schedulerJobService.saveOrUpdate(scheduleJobEntity);
                ScheduleUtils.createScheduleJob(scheduler, scheduleJobEntity);
            }

            scheduler.start();

        } catch (SchedulerException e) {
            e.printStackTrace();
        }

        if (initStart) {
            logger.info("已设置前期延期任务");
            AutoLoginTask autoLoginTask = (AutoLoginTask) SpringContextUtils.getBean("autoLoginTask");
            autoLoginTask.run(null);
        }
    }

    private void initializeCache() {
        taobaoAccountProvider.initialize();
    }
}
