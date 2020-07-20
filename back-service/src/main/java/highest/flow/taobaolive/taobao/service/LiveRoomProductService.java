package highest.flow.taobaolive.taobao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import highest.flow.taobaolive.taobao.entity.MemberTaoAccEntity;
import highest.flow.taobaolive.taobao.entity.PreLiveRoomSpecEntity;
import highest.flow.taobaolive.taobao.entity.ProductEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface LiveRoomProductService extends IService<ProductEntity> {

    List<ProductEntity> getProducts(String liveId);

    void saveProducts(String liveId, List<ProductEntity> products);
}
