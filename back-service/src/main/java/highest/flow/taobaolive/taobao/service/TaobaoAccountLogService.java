package highest.flow.taobaolive.taobao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import highest.flow.taobaolive.api.param.PageParam;
import highest.flow.taobaolive.common.utils.PageUtils;
import highest.flow.taobaolive.sys.entity.SysMember;
import highest.flow.taobaolive.taobao.entity.TaobaoAccountEntity;
import highest.flow.taobaolive.taobao.entity.TaobaoAccountLogEntity;
import org.apache.http.cookie.Cookie;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public interface TaobaoAccountLogService extends IService<TaobaoAccountLogEntity> {

    PageUtils queryPage(SysMember sysMember, PageParam pageParam);
}
