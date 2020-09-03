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

    int getNormalCount(SysMember sysMember, PageParam pageParam);

    int getExpiredCount(SysMember sysMember, PageParam pageParam);

    PageUtils queryPage(SysMember sysMember, PageParam pageParam);

    /**
     * 只返回昵称和uid, 状态
     * @param sysMember
     * @param pageParam
     * @return
     */
    PageUtils simpleQueryPage(SysMember sysMember, PageParam pageParam);

    List<TaobaoAccountEntity> getActivesByMember(SysMember sysMember, int count);

    List<TaobaoAccountEntity> getActiveAll();

    List<TaobaoAccountEntity> getActiveAllByMember(SysMember sysMember);
}
