package highest.flow.taobaolive.taobao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.taobao.entity.LiveRoomStrategyEntity;
import highest.flow.taobaolive.taobao.entity.MemberTaoAccEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface LiveRoomStrategyService extends IService<LiveRoomStrategyEntity> {

    boolean setTask(MemberTaoAccEntity memberTaoAccEntity, List<LiveRoomStrategyEntity> liveRoomStrategyEntities);

    List<LiveRoomStrategyEntity> getLiveRoomStrategies(String taobaoAccountNick);
}
