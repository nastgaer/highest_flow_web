package highest.flow.taobaolive.taobao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import highest.flow.taobaolive.taobao.dao.ProductDao;
import highest.flow.taobaolive.taobao.entity.ProductEntity;
import highest.flow.taobaolive.taobao.service.LiveRoomProductService;
import org.springframework.stereotype.Service;

@Service("liveRoomProductService")
public class LiveRoomProductServiceImpl extends ServiceImpl<ProductDao, ProductEntity> implements LiveRoomProductService {
}
