package highest.flow.taobaolive.job.utils;

import highest.flow.taobaolive.common.utils.SpringContextUtils;
import highest.flow.taobaolive.job.entity.ScheduleJobEntity;
import highest.flow.taobaolive.job.entity.ScheduleJobLogEntity;
import highest.flow.taobaolive.job.service.ScheduleJobLogService;
import org.apache.commons.lang.StringUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.lang.reflect.Method;
import java.util.Date;

public class ScheduleJob extends QuartzJobBean {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        ScheduleJobEntity scheduleJob = (ScheduleJobEntity)jobExecutionContext.getMergedJobDataMap().get(ScheduleJobEntity.JOB_PARAM_KEY);

        // 获取spring bean
        ScheduleJobLogService scheduleJobLogService = (ScheduleJobLogService) SpringContextUtils.getBean("scheduleJobLogService");

        ScheduleJobLogEntity log = new ScheduleJobLogEntity();
        log.setJobId(scheduleJob.getId());
        log.setBeanName(scheduleJob.getBeanName());
        log.setParams(scheduleJob.getParams());
        log.setCreatedTime(new Date());

        // 任务开始时间
        long startTime = System.currentTimeMillis();

        try {
            // 执行任务
            logger.debug("任务准备执行，任务ID：" + scheduleJob.getId());

            Object target = SpringContextUtils.getBean(scheduleJob.getBeanName());
            Method method = target.getClass().getDeclaredMethod("run", String.class);
            method.invoke(target, scheduleJob.getParams());

            // 任务执行总时长
            long times = System.currentTimeMillis() - startTime;
            log.setTimes((int) times);
            // 任务状态 0：成功 1：失败
            log.setStatus(0);

            logger.debug("任务执行完毕，任务ID：" + scheduleJob.getJobId() + "  总共耗时：" + times + "毫秒");

        } catch (Exception ex) {
            logger.error("任务执行失败，任务ID：" + scheduleJob.getJobId(), e);

            // 任务执行总时长
            long times = System.currentTimeMillis() - startTime;
            log.setTimes((int) times);

            // 任务状态 0：成功 1：失败
            log.setStatus(1);
            log.setError(StringUtils.substring(e.toString(), 0, 2000));
        } finally {
            scheduleJobLogService.save(log);
        }
    }
}
