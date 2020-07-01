package highest.flow.taobaolive.taobao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import highest.flow.taobaolive.taobao.dao.ProductDao;
import highest.flow.taobaolive.taobao.dao.TemplateDao;
import highest.flow.taobaolive.taobao.entity.ProductEntity;
import highest.flow.taobaolive.taobao.entity.TemplateEntity;
import highest.flow.taobaolive.taobao.service.LiveRoomProductService;
import highest.flow.taobaolive.taobao.service.TemplateService;
import org.springframework.stereotype.Service;

@Service("templateService")
public class TemplateServiceImpl extends ServiceImpl<TemplateDao, TemplateEntity> implements TemplateService {
}
