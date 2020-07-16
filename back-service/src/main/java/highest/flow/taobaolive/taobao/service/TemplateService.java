package highest.flow.taobaolive.taobao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import highest.flow.taobaolive.api.param.PageParam;
import highest.flow.taobaolive.common.utils.PageUtils;
import highest.flow.taobaolive.taobao.entity.ProductEntity;
import highest.flow.taobaolive.taobao.entity.TemplateEntity;
import org.springframework.stereotype.Service;

@Service
public interface TemplateService extends IService<TemplateEntity> {

    PageUtils queryPage(PageParam pageParam);
}
