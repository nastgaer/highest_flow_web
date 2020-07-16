package highest.flow.taobaolive.taobao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import highest.flow.taobaolive.api.param.PageParam;
import highest.flow.taobaolive.common.utils.HFStringUtils;
import highest.flow.taobaolive.common.utils.PageUtils;
import highest.flow.taobaolive.common.utils.Query;
import highest.flow.taobaolive.sys.entity.SysMember;
import highest.flow.taobaolive.taobao.dao.MemberTaobaoAccountDao;
import highest.flow.taobaolive.taobao.entity.MemberTaoAccEntity;
import highest.flow.taobaolive.taobao.service.MemberTaoAccService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("memberTaobaoAccountService")
public class MemberTaoAccServiceImpl extends ServiceImpl<MemberTaobaoAccountDao, MemberTaoAccEntity> implements MemberTaoAccService {

    @Override
    public PageUtils queryPage(PageParam pageParam) {
        int pageNo = pageParam.getPageNo();
        int pageSize = pageParam.getPageSize();
        String keyword = pageParam.getKeyword();

        Map<String, Object> params = new HashMap<>();
        params.put(Query.PAGE, pageNo);
        params.put(Query.LIMIT, pageSize);

        QueryWrapper<MemberTaoAccEntity> queryWrapper = new QueryWrapper<>();
        if (!HFStringUtils.isNullOrEmpty(keyword)) {
            queryWrapper.like("room_name", keyword)
                    .or()
                    .like("taobao_account_nick", keyword);
        }

        IPage<MemberTaoAccEntity> page = this.page(new Query<MemberTaoAccEntity>().getPage(params), queryWrapper);
        return new PageUtils<MemberTaoAccEntity>(page);
    }

    @Override
    public MemberTaoAccEntity getMemberByTaobaoAccountNick(String nick) {
        return this.getOne(Wrappers.<MemberTaoAccEntity>lambdaQuery()
                .eq(MemberTaoAccEntity::getTaobaoAccountNick, nick));
    }
}
