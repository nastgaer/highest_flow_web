package highest.flow.taobaolive.taobao.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import highest.flow.taobaolive.taobao.dao.PreLiveRoomSpecDao;
import highest.flow.taobaolive.taobao.entity.PreLiveRoomSpecEntity;
import highest.flow.taobaolive.taobao.service.PreLiveRoomSpecService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("preLiveRoomSpecService")
public class PreLiveRoomSpecServiceImpl extends ServiceImpl<PreLiveRoomSpecDao, PreLiveRoomSpecEntity> implements PreLiveRoomSpecService {

    @Override
    public List<PreLiveRoomSpecEntity> getPreLiveRoomSpecs(String taobaoAccountNiсk) {
        return this.list(Wrappers.<PreLiveRoomSpecEntity>lambdaQuery()
                .eq(PreLiveRoomSpecEntity::getTaobaoAccountNick, taobaoAccountNiсk));
    }
}
