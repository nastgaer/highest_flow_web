package highest.flow.taobaolive.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import highest.flow.taobaolive.api.param.PageParam;
import highest.flow.taobaolive.common.utils.HFStringUtils;
import highest.flow.taobaolive.common.utils.PageUtils;
import highest.flow.taobaolive.common.utils.Query;
import highest.flow.taobaolive.sys.dao.SysLogDao;
import highest.flow.taobaolive.sys.entity.SysLogEntity;
import highest.flow.taobaolive.sys.service.SysLogService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service("sysLogService")
public class SysLogServiceImpl extends ServiceImpl<SysLogDao, SysLogEntity> implements SysLogService {

    @Override
    public PageUtils queryPage(PageParam pageParam) {
        int pageNo = pageParam.getPageNo();
        int pageSize = pageParam.getPageSize();
        String keyword = pageParam.getKeyword();

        Map<String, Object> params = new HashMap<>();
        params.put(Query.PAGE, pageNo);
        params.put(Query.LIMIT, pageSize);

        QueryWrapper<SysLogEntity> queryWrapper = new QueryWrapper<>();
        if (!HFStringUtils.isNullOrEmpty(keyword)) {
            queryWrapper.like("member_name", keyword).or()
                    .like("operation", keyword).or()
                    .like("method", keyword).or()
                    .like("params", keyword);
        }

        IPage<SysLogEntity> page = this.page(new Query<SysLogEntity>().getPage(params), queryWrapper);
        return new PageUtils<SysLogEntity>(page);
    }
}
