package highest.flow.taobaolive.taobao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import highest.flow.taobaolive.taobao.entity.TaobaoAccount;
import org.springframework.stereotype.Service;

@Service
public interface TaobaoAccountService extends IService<TaobaoAccount> {

    public TaobaoAccount register(String accountId, String nick,
                                  String sid,
                                  String utdid,
                                  String devid,
                                  String autoLoginToken,
                                  String cookie,
                                  int expires);
}
