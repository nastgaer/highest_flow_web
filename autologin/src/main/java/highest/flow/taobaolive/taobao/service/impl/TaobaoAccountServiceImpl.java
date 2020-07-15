package highest.flow.taobaolive.taobao.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import highest.flow.taobaolive.common.defines.ErrorCodes;
import highest.flow.taobaolive.taobao.dao.TaobaoAccountDao;
import highest.flow.taobaolive.taobao.defines.TaobaoAccountLogKind;
import highest.flow.taobaolive.taobao.defines.TaobaoAccountState;
import highest.flow.taobaolive.taobao.entity.TaobaoAccountEntity;
import highest.flow.taobaolive.taobao.entity.TaobaoAccountLogEntity;
import highest.flow.taobaolive.taobao.service.TaobaoAccountLogService;
import highest.flow.taobaolive.taobao.service.TaobaoAccountService;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service("taobaoAccountService")
public class TaobaoAccountServiceImpl extends ServiceImpl<TaobaoAccountDao, TaobaoAccountEntity> implements TaobaoAccountService {

    @Autowired
    private TaobaoAccountLogService taobaoAccountLogService;

    @Override
    public TaobaoAccountEntity register(String nick, String uid, String sid, String utdid, String devid,
                                        String autoLoginToken, String umidToken, List<Cookie> cookies, long expires, int state,
                                        Date created, Date updated) {
        try {
            TaobaoAccountEntity taobaoAccountEntity = new TaobaoAccountEntity();

            taobaoAccountEntity.setNick(nick);
            taobaoAccountEntity.setUid(uid);
            taobaoAccountEntity.setSid(sid);
            taobaoAccountEntity.setUtdid(utdid);
            taobaoAccountEntity.setDevid(devid);
            taobaoAccountEntity.setAutoLoginToken(autoLoginToken);
            taobaoAccountEntity.setUmidToken(umidToken);

            CookieStore cookieStore = new BasicCookieStore();
            for (Cookie cookie : cookies) {
                cookieStore.addCookie(cookie);
            }
            taobaoAccountEntity.setCookieStore(cookieStore);

            Date expireDate = new Date();
            expireDate.setTime(expireDate.getTime() + expires);
            taobaoAccountEntity.setExpires(expireDate);
            taobaoAccountEntity.setState(TaobaoAccountState.fromInt(state).getState());
            taobaoAccountEntity.setCreatedTime(created);
            taobaoAccountEntity.setUpdatedTime(updated);

            TaobaoAccountEntity selected = this.getOne(Wrappers.<TaobaoAccountEntity>lambdaQuery().eq(TaobaoAccountEntity::getUid, uid));
            if (selected != null) {
                taobaoAccountEntity.setId(selected.getId());
                this.updateById(taobaoAccountEntity);
            } else {
                this.save(taobaoAccountEntity);
            }

            TaobaoAccountLogEntity taobaoAccountLogEntity = new TaobaoAccountLogEntity();
            taobaoAccountLogEntity.setKind(selected != null ? TaobaoAccountLogKind.Update.getKind() : TaobaoAccountLogKind.New.getKind());
            taobaoAccountLogEntity.setUid(taobaoAccountEntity.getUid());
            taobaoAccountLogEntity.setNick(taobaoAccountEntity.getNick());
            taobaoAccountLogEntity.setSuccess(1);
            taobaoAccountLogEntity.setExpires(taobaoAccountEntity.getExpires());
            taobaoAccountLogEntity.setContent("");
            taobaoAccountLogEntity.setCreatedTime(new Date());
            taobaoAccountLogService.save(taobaoAccountLogEntity);
            return taobaoAccountEntity;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public TaobaoAccountEntity getInfo(String nick) {
        return baseMapper.selectOne(Wrappers.<TaobaoAccountEntity>lambdaQuery().eq(TaobaoAccountEntity::getNick, nick));
    }
}
