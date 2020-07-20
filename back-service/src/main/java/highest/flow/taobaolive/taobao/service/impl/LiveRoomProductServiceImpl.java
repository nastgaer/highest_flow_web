package highest.flow.taobaolive.taobao.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import highest.flow.taobaolive.taobao.dao.ProductDao;
import highest.flow.taobaolive.taobao.entity.ProductEntity;
import highest.flow.taobaolive.taobao.service.LiveRoomProductService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("liveRoomProductService")
public class LiveRoomProductServiceImpl extends ServiceImpl<ProductDao, ProductEntity> implements LiveRoomProductService {

    @Override
    public List<ProductEntity> getProducts(String liveId) {
         return this.list(Wrappers.<ProductEntity>lambdaQuery().eq(ProductEntity::getLiveId, liveId));
    }

    @Override
    public void saveProducts(String liveId, List<ProductEntity> products) {
        for (ProductEntity productEntity : products) {
            productEntity.setLiveId(liveId);
        }
        this.saveOrUpdateBatch(products);
    }
}
