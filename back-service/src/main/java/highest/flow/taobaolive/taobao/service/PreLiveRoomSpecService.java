package highest.flow.taobaolive.taobao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import highest.flow.taobaolive.taobao.entity.PreLiveRoomSpecEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PreLiveRoomSpecService extends IService<PreLiveRoomSpecEntity> {

    List<PreLiveRoomSpecEntity> getPreLiveRoomSpecs(String taobaoAccountNiсk);
}
