package highest.flow.taobaolive.taobao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import highest.flow.taobaolive.api.param.PageParam;
import highest.flow.taobaolive.common.utils.HFStringUtils;
import highest.flow.taobaolive.common.utils.PageUtils;
import highest.flow.taobaolive.common.utils.Query;
import highest.flow.taobaolive.taobao.dao.ProductDao;
import highest.flow.taobaolive.taobao.dao.TemplateDao;
import highest.flow.taobaolive.taobao.entity.ProductEntity;
import highest.flow.taobaolive.taobao.entity.TemplateEntity;
import highest.flow.taobaolive.taobao.service.LiveRoomProductService;
import highest.flow.taobaolive.taobao.service.TemplateService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service("templateService")
public class TemplateServiceImpl extends ServiceImpl<TemplateDao, TemplateEntity> implements TemplateService {

    @Override
    public PageUtils queryPage(PageParam pageParam) {
        int pageNo = pageParam.getPageNo();
        int pageSize = pageParam.getPageSize();
        String keyword = pageParam.getKeyword();

        Map<String, Object> params = new HashMap<>();
        params.put(Query.PAGE, pageNo);
        params.put(Query.LIMIT, pageSize);

        QueryWrapper<TemplateEntity> queryWrapper = new QueryWrapper<>();
        if (!HFStringUtils.isNullOrEmpty(keyword)) {
            queryWrapper.like("template_name", keyword);
        }

        IPage<TemplateEntity> page = this.page(new Query<TemplateEntity>().getPage(params), queryWrapper);
        return new PageUtils<TemplateEntity>(page);
    }
}
