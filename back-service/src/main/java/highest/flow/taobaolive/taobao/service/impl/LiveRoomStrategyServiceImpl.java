package highest.flow.taobaolive.taobao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import highest.flow.taobaolive.taobao.dao.LiveRoomStrategyDao;
import highest.flow.taobaolive.taobao.dao.ProductDao;
import highest.flow.taobaolive.taobao.entity.LiveRoomStrategyEntity;
import highest.flow.taobaolive.taobao.entity.ProductEntity;
import highest.flow.taobaolive.taobao.service.LiveRoomProductService;
import highest.flow.taobaolive.taobao.service.LiveRoomStrategyService;
import org.springframework.stereotype.Service;

@Service("liveRoomStrategyService")
public class LiveRoomStrategyServiceImpl extends ServiceImpl<LiveRoomStrategyDao, LiveRoomStrategyEntity> implements LiveRoomStrategyService {
}
