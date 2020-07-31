package highest.flow.taobaolive.taobao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.taobao.entity.LiveRoomStrategyEntity;
import highest.flow.taobaolive.taobao.entity.MemberTaoAccEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface LiveRoomStrategyService extends IService<LiveRoomStrategyEntity> {

    /**
     * 返回执行淘宝账号的引流操作内容
     * @param memberTaoAccEntity
     * @return
     */
    List<LiveRoomStrategyEntity> getLiveRoomStrategies(MemberTaoAccEntity memberTaoAccEntity);

    boolean setTask(MemberTaoAccEntity memberTaoAccEntity, List<LiveRoomStrategyEntity> liveRoomStrategyEntities);

    // 续费操作
    boolean resumeTask(MemberTaoAccEntity memberTaoAccEntity);

    // 停止服务
    boolean stopTask(MemberTaoAccEntity memberTaoAccEntity);
}
