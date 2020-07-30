package highest.flow.taobaolive.taobao.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import highest.flow.taobaolive.common.utils.HFStringUtils;
import highest.flow.taobaolive.taobao.dao.ProductDao;
import highest.flow.taobaolive.taobao.entity.LiveRoomEntity;
import highest.flow.taobaolive.taobao.entity.ProductEntity;
import highest.flow.taobaolive.taobao.service.LiveRoomProductService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("liveRoomProductService")
public class LiveRoomProductServiceImpl extends ServiceImpl<ProductDao, ProductEntity> implements LiveRoomProductService {

    @Override
    public List<ProductEntity> getProducts(LiveRoomEntity liveRoomEntity) {
        if (!HFStringUtils.isNullOrEmpty(liveRoomEntity.getLiveId())) {
            return this.list(Wrappers.<ProductEntity>lambdaQuery().eq(ProductEntity::getLiveId, liveRoomEntity.getLiveId()));
        }
        return this.list(Wrappers.<ProductEntity>lambdaQuery().eq(ProductEntity::getHistoryId, liveRoomEntity.getId()));
    }

    @Override
    public void saveProducts(List<ProductEntity> products) {
        if (products.size() < 1) {
            return;
        }
        this.saveOrUpdateBatch(products);
    }
}
