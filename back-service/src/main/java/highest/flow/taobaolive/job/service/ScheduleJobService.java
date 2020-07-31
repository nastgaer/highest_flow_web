package highest.flow.taobaolive.job.service;

import com.baomidou.mybatisplus.extension.service.IService;
import highest.flow.taobaolive.job.entity.ScheduleJobEntity;
import org.springframework.stereotype.Service;

@Service
public interface ScheduleJobService extends IService<ScheduleJobEntity> {

    /**
     * 保存定时任务
     */
    void saveJob(ScheduleJobEntity scheduleJob);

    /**
     * 更新定时任务
     */
    void updateJob(ScheduleJobEntity scheduleJob);

    /**
     * 立即执行
     */
    void runJob(ScheduleJobEntity scheduleJob);

    /**
     * 暂停运行
     */
    void stopJob(ScheduleJobEntity scheduleJob);

    /**
     * 删除定时任务
     */
    void deleteJob(ScheduleJobEntity scheduleJob);

    /**
     * 恢复运行
     */
    void resumeJob(ScheduleJobEntity scheduleJob);

    ScheduleJobEntity findRunnigJob(String beanName, String param);

    ScheduleJobEntity findScheduledJob(String beanName, String param);
}
