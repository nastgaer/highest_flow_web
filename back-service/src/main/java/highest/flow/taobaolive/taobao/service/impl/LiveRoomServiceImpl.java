package highest.flow.taobaolive.taobao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import highest.flow.taobaolive.taobao.dao.LiveRoomDao;
import highest.flow.taobaolive.taobao.entity.LiveRoomEntity;
import highest.flow.taobaolive.taobao.service.LiveRoomService;
import org.springframework.stereotype.Service;

@Service("liveRoomService")
public class LiveRoomServiceImpl extends ServiceImpl<LiveRoomDao, LiveRoomEntity> implements LiveRoomService {
}
