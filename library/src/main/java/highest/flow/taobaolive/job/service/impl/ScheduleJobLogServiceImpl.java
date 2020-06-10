package highest.flow.taobaolive.job.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import highest.flow.taobaolive.job.dao.ScheduleJobDao;
import highest.flow.taobaolive.job.dao.ScheduleJobLogDao;
import highest.flow.taobaolive.job.entity.ScheduleJobEntity;
import highest.flow.taobaolive.job.entity.ScheduleJobLogEntity;
import highest.flow.taobaolive.job.service.ScheduleJobLogService;
import highest.flow.taobaolive.job.service.ScheduleJobService;
import org.springframework.stereotype.Service;

@Service("scheduleJobLogService")
public class ScheduleJobLogServiceImpl extends ServiceImpl<ScheduleJobLogDao, ScheduleJobLogEntity> implements ScheduleJobLogService {

}
