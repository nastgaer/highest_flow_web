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

    PageUtils queryPage(SysMember sysMember, PageParam pageParam);

    /**
     * 返回正常小号数
     * @param sysMember
     * @param pageParam
     * @return
     */
    int getNormalCount(SysMember sysMember, PageParam pageParam);

    /**
     * 返回过期的小号数
     * @param sysMember
     * @param pageParam
     * @return
     */
    int getExpiredCount(SysMember sysMember, PageParam pageParam);

    /**
     * 新注册小号, 如果已经注册的，就更新内容
     * @param sysMember
     * @param nick
     * @param uid
     * @param sid
     * @param utdid
     * @param devid
     * @param autoLoginToken
     * @param umidToken
     * @param cookies
     * @param expires
     * @param state
     * @param created
     * @param updated
     * @return
     */
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

    /**
     * 根据小号昵称查询小号
     * @param nick
     * @return
     */
    TaobaoAccountEntity getInfo(String nick);

    /**
     * 根据uid查询兄啊好
     * @param uid
     * @return
     */
    TaobaoAccountEntity getInfoByUid(String uid);

    /**
     * 查询指定会员的小号列表
     * @param sysMember
     * @param count
     * @return
     */
    List<TaobaoAccountEntity> getActivesByMember(SysMember sysMember, int count);

    /**
     * 返回缓冲的所有正常的小号列表
     * @return
     */
    List<TaobaoAccountEntity> getActiveAll();

    /**
     * 更新缓冲小号
     * @param taobaoAccountEntity
     */
    void cacheAccount(TaobaoAccountEntity taobaoAccountEntity);

    /**
     * 返回最后缓冲的时间
     * @return
     */
    Date getLastUpdated();

    /**
     * 把指定时间以后更新的小号全部更新
     * @param updated
     * @return 返回更新的小号数
     */
    int reloadUpdatedAccounts(Date updated);
}
