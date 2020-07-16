package highest.flow.taobaolive.taobao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import highest.flow.taobaolive.taobao.entity.LiveRoomEntity;
import highest.flow.taobaolive.taobao.entity.LiveRoomStrategyEntity;
import highest.flow.taobaolive.taobao.entity.MemberTaoAccEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public interface LiveRoomHistoryService extends IService<LiveRoomEntity> {

    List<LiveRoomEntity> getTodays(Date today);
}
