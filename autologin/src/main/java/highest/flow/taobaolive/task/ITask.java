package highest.flow.taobaolive.task;

import highest.flow.taobaolive.job.entity.ScheduleJobEntity;

public interface ITask {

    void run(ScheduleJobEntity scheduleJobEntity);
}
