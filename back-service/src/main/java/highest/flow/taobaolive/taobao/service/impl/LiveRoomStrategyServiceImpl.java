package highest.flow.taobaolive.taobao.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.job.defines.ScheduleState;
import highest.flow.taobaolive.job.entity.ScheduleJobEntity;
import highest.flow.taobaolive.job.service.ScheduleJobService;
import highest.flow.taobaolive.job.utils.ScheduleUtils;
import highest.flow.taobaolive.taobao.dao.LiveRoomStrategyDao;
import highest.flow.taobaolive.taobao.dao.ProductDao;
import highest.flow.taobaolive.taobao.entity.LiveRoomStrategyEntity;
import highest.flow.taobaolive.taobao.entity.MemberTaoAccEntity;
import highest.flow.taobaolive.taobao.entity.ProductEntity;
import highest.flow.taobaolive.taobao.service.LiveRoomProductService;
import highest.flow.taobaolive.taobao.service.LiveRoomStrategyService;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service("liveRoomStrategyService")
public class LiveRoomStrategyServiceImpl extends ServiceImpl<LiveRoomStrategyDao, LiveRoomStrategyEntity> implements LiveRoomStrategyService {

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private ScheduleJobService schedulerJobService;

    @Override
    public boolean setTask(MemberTaoAccEntity memberTaoAccEntity, List<LiveRoomStrategyEntity> liveRoomStrategyEntities) {
        try {
            String taobaoAccountNick = memberTaoAccEntity.getTaobaoAccountNick();

            LiveRoomStrategyEntity deleteEntity = new LiveRoomStrategyEntity();
            deleteEntity.setIsdel(true);
            deleteEntity.setUpdatedTime(new Date());
            this.baseMapper.update(deleteEntity, Wrappers.<LiveRoomStrategyEntity>lambdaQuery().eq(LiveRoomStrategyEntity::getTaobaoAccountNick, taobaoAccountNick));

            for (LiveRoomStrategyEntity liveRoomStrategyEntity : liveRoomStrategyEntities) {
                liveRoomStrategyEntity.setTaobaoAccountNick(taobaoAccountNick);
                liveRoomStrategyEntity.setIsdel(false);
                liveRoomStrategyEntity.setCreatedTime(new Date());
                liveRoomStrategyEntity.setUpdatedTime(new Date());
            }

            this.saveBatch(liveRoomStrategyEntities);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(memberTaoAccEntity.getOperationStartTime());
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            int second = calendar.get(Calendar.SECOND);

            String expression = String.format("%d %d %d 1/1 * ? *", second, minute, hour);

            this.schedulerJobService.remove(Wrappers.<ScheduleJobEntity>lambdaQuery()
                    .eq(ScheduleJobEntity::getParams, taobaoAccountNick));

            ScheduleJobEntity scheduleJobEntity = new ScheduleJobEntity();
            scheduleJobEntity.setBeanName("batchLiveRoomTask");
            scheduleJobEntity.setParams(taobaoAccountNick);
            scheduleJobEntity.setCronExpression(expression);
            scheduleJobEntity.setCreatedTime(new Date());
            scheduleJobEntity.setState(ScheduleState.NORMAL.getValue());
            scheduleJobEntity.setRemark("批量发布预告任务");

            this.schedulerJobService.saveOrUpdate(scheduleJobEntity);
            ScheduleUtils.createScheduleJob(scheduler, scheduleJobEntity);

            return true;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public List<LiveRoomStrategyEntity> getLiveRoomStrategies(String taobaoAccountNick) {
        return this.list(Wrappers.<LiveRoomStrategyEntity>lambdaQuery().eq(LiveRoomStrategyEntity::getTaobaoAccountNick, taobaoAccountNick)
                .eq(LiveRoomStrategyEntity::isIsdel, false));
    }
}
