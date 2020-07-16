package highest.flow.taobaolive.taobao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import highest.flow.taobaolive.api.param.PageParam;
import highest.flow.taobaolive.common.utils.PageUtils;
import highest.flow.taobaolive.taobao.entity.MemberTaoAccEntity;
import org.springframework.stereotype.Service;

@Service
public interface MemberTaoAccService extends IService<MemberTaoAccEntity> {

    PageUtils queryPage(PageParam pageParam);

    MemberTaoAccEntity getMemberByTaobaoAccountNick(String nick);
}
