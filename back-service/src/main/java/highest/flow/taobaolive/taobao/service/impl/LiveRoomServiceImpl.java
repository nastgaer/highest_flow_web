package highest.flow.taobaolive.taobao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import highest.flow.taobaolive.api.param.PageParam;
import highest.flow.taobaolive.common.utils.HFStringUtils;
import highest.flow.taobaolive.common.utils.PageUtils;
import highest.flow.taobaolive.common.utils.Query;
import highest.flow.taobaolive.taobao.dao.LiveRoomDao;
import highest.flow.taobaolive.taobao.entity.LiveRoomEntity;
import highest.flow.taobaolive.taobao.service.LiveRoomService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service("liveRoomService")
public class LiveRoomServiceImpl extends ServiceImpl<LiveRoomDao, LiveRoomEntity> implements LiveRoomService {

    @Override
    public PageUtils queryPage(PageParam pageParam) {
        int pageNo = pageParam.getPageNo();
        int pageSize = pageParam.getPageSize();
        String keyword = pageParam.getKeyword();

        Map<String, Object> params = new HashMap<>();
        params.put(Query.PAGE, pageNo);
        params.put(Query.LIMIT, pageSize);

        QueryWrapper<LiveRoomEntity> queryWrapper = new QueryWrapper<>();
        if (!HFStringUtils.isNullOrEmpty(keyword)) {
            queryWrapper.like("taobao_account_nick", keyword).or()
                    .like("account_name", keyword);
        }

        IPage<LiveRoomEntity> page = this.page(new Query<LiveRoomEntity>().getPage(params), queryWrapper);
        return new PageUtils<LiveRoomEntity>(page);
    }
}
