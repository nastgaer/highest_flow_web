package highest.flow.taobaolive.taobao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import highest.flow.taobaolive.taobao.dao.MemberTaobaoAccountDao;
import highest.flow.taobaolive.taobao.entity.MemberTaoAccEntity;
import highest.flow.taobaolive.taobao.service.MemberTaoAccService;
import org.springframework.stereotype.Service;

@Service("memberTaobaoAccountService")
public class MemberTaoAccServiceImpl extends ServiceImpl<MemberTaobaoAccountDao, MemberTaoAccEntity> implements MemberTaoAccService {
}
