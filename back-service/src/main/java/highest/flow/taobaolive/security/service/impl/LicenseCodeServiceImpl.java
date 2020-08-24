package highest.flow.taobaolive.security.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import highest.flow.taobaolive.api.param.PageParam;
import highest.flow.taobaolive.common.utils.HFStringUtils;
import highest.flow.taobaolive.common.utils.PageUtils;
import highest.flow.taobaolive.common.utils.Query;
import highest.flow.taobaolive.security.dao.LicenseCodeDao;
import highest.flow.taobaolive.security.entity.LicenseCode;
import highest.flow.taobaolive.security.service.LicenseCodeService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service("licenseCodeService")
public class LicenseCodeServiceImpl extends ServiceImpl<LicenseCodeDao, LicenseCode> implements LicenseCodeService {

    @Override
    public PageUtils queryPage(PageParam pageParam) {
        int pageNo = pageParam.getPageNo();
        int pageSize = pageParam.getPageSize();
        String keyword = pageParam.getKeyword();

        Map<String, Object> params = new HashMap<>();
        params.put(Query.PAGE, pageNo);
        params.put(Query.LIMIT, pageSize);

        QueryWrapper<LicenseCode> queryWrapper = new QueryWrapper<>();
        if (!HFStringUtils.isNullOrEmpty(keyword)) {
            queryWrapper.like("code", keyword)
                    .or()
                    .like("member_code", keyword);
        }

        IPage<LicenseCode> page = this.page(new Query<LicenseCode>().getPage(params), queryWrapper);
        return new PageUtils<LicenseCode>(page);
    }

    @Override
    public LicenseCode getCodeDesc(String code) {
        return this.baseMapper.getCodeDesc(code);
    }
}
