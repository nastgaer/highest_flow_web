package highest.flow.taobaolive.xiaohao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import highest.flow.taobaolive.xiaohao.dao.TaobaoAccountDao;
import highest.flow.taobaolive.xiaohao.entity.TaobaoAccount;
import highest.flow.taobaolive.xiaohao.service.TaobaoAccountService;
import org.springframework.stereotype.Service;

@Service("taobaoAccountService")
public class TaobaoAccountServiceImpl extends ServiceImpl<TaobaoAccountDao, TaobaoAccount> implements TaobaoAccountService {

}
