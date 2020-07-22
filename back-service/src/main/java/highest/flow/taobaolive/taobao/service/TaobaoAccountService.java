package highest.flow.taobaolive.taobao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import highest.flow.taobaolive.api.param.PageParam;
import highest.flow.taobaolive.common.utils.PageUtils;
import highest.flow.taobaolive.sys.entity.SysMember;
import highest.flow.taobaolive.taobao.entity.TaobaoAccountEntity;
import org.apache.http.cookie.Cookie;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public interface TaobaoAccountService extends IService<TaobaoAccountEntity> {

    TaobaoAccountEntity register(SysMember sysMember,
                                        String nick,
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

    TaobaoAccountEntity getInfo(String nick);

    TaobaoAccountEntity getInfoByUid(String uid);

    int getNormalCount(SysMember sysMember);

    int getExpiredCount(SysMember sysMember);

    PageUtils queryPage(SysMember sysMember, PageParam pageParam);

    TaobaoAccountEntity getActiveOne(SysMember sysMember);

    List<TaobaoAccountEntity> getActiveAllByMember(SysMember sysMember);
}
