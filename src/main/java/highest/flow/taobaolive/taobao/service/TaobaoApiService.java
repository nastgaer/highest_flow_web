package highest.flow.taobaolive.taobao.service;

import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.taobao.entity.TaobaoAccount;
import org.springframework.stereotype.Service;

@Service
public interface TaobaoApiService {

    public R GetUserSimple(TaobaoAccount taobaoAccount);

    public R AutoLogin(TaobaoAccount taobaoAccount);
}