package highest.flow.taobaolive.taobao.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import highest.flow.taobaolive.job.defines.ScheduleState;
import highest.flow.taobaolive.job.entity.ScheduleJobEntity;
import highest.flow.taobaolive.job.utils.ScheduleUtils;
import highest.flow.taobaolive.taobao.dao.LiveRoomDao;
import highest.flow.taobaolive.taobao.dao.LiveRoomStrategyDao;
import highest.flow.taobaolive.taobao.entity.LiveRoomEntity;
import highest.flow.taobaolive.taobao.entity.LiveRoomStrategyEntity;
import highest.flow.taobaolive.taobao.entity.MemberTaoAccEntity;
import highest.flow.taobaolive.taobao.service.LiveRoomHistoryService;
import highest.flow.taobaolive.taobao.service.LiveRoomStrategyService;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service("liveRoomHistoryService")
public class LiveRoomHistoryServiceImpl extends ServiceImpl<LiveRoomDao, LiveRoomEntity> implements LiveRoomHistoryService {

    @Override
    public List<LiveRoomEntity> getTodays(Date todayDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String today = sdf.format(todayDate);
        return this.baseMapper.queryTodays(today);
    }
}
