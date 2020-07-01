package highest.flow.taobaolive.taobao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import highest.flow.taobaolive.taobao.dao.LiveRoomDao;
import highest.flow.taobaolive.taobao.dao.PreLiveTemplateDao;
import highest.flow.taobaolive.taobao.entity.LiveRoomEntity;
import highest.flow.taobaolive.taobao.entity.PreLiveTemplateEntity;
import highest.flow.taobaolive.taobao.service.LiveRoomService;
import highest.flow.taobaolive.taobao.service.PreLiveTemplateService;
import org.springframework.stereotype.Service;

@Service("preLiveTemplateService")
public class PreLiveTemplateServiceImpl extends ServiceImpl<PreLiveTemplateDao, PreLiveTemplateEntity> implements PreLiveTemplateService {
}
