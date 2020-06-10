package highest.flow.taobaolive.job.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import highest.flow.taobaolive.job.entity.ScheduleJobEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ScheduleJobDao extends BaseMapper<ScheduleJobEntity> {
}
