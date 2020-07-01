package highest.flow.taobaolive.taobao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import highest.flow.taobaolive.taobao.entity.TaobaoAccountEntity;
import org.apache.http.cookie.Cookie;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public interface TaobaoAccountService extends IService<TaobaoAccountEntity> {

    public TaobaoAccountEntity register(String accountId, String nick,
                                        String uid,
                                        String sid,
                                        String utdid,
                                        String devid,
                                        String autoLoginToken,
                                        String umidToken,
                                        List<Cookie> cookies,
                                        long expires,
                                        int state,
                                        Date created,
                                        Date updated);

    public TaobaoAccountEntity getInfo(String accountId);
}
