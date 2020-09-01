package highest.flow.taobaolive;

import highest.flow.taobaolive.job.defines.ScheduleState;
import highest.flow.taobaolive.job.entity.ScheduleJobEntity;
import highest.flow.taobaolive.job.service.ScheduleJobService;
import highest.flow.taobaolive.job.utils.ScheduleUtils;
import highest.flow.taobaolive.taobao.service.TaobaoAccountService;
import org.quartz.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private TaobaoAccountService taobaoAccountService;

    @Override
    public void run(String... args) throws Exception {
        logger.info("初始化开始了");

        initializeJob();

        initializeCache();

        logger.info("初始化结束了");
    }

    private void initializeJob() {
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
                scheduleJobEntity.setCronExpression("0 0 0 ? * * *");
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

    private void initializeCache() {
        try {
            // 该函数经常用，所以打开第一时间获取掉
            taobaoAccountService.getActiveAll();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
