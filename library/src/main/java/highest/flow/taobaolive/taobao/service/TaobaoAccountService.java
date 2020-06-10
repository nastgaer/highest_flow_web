package highest.flow.taobaolive.taobao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import highest.flow.taobaolive.taobao.entity.TaobaoAccount;
import org.apache.http.cookie.Cookie;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TaobaoAccountService extends IService<TaobaoAccount> {

    public TaobaoAccount register(String accountId, String nick,
                                  String sid,
                                  String utdid,
                                  String devid,
                                  String autoLoginToken,
                                  List<Cookie> cookies,
                                  int expires);

    public TaobaoAccount getInfo(String accountId);
}
