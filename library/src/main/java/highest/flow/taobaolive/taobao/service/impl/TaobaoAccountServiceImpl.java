package highest.flow.taobaolive.taobao.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import highest.flow.taobaolive.common.http.CookieHelper;
import highest.flow.taobaolive.taobao.dao.TaobaoAccountDao;
import highest.flow.taobaolive.taobao.defines.TaobaoAccountState;
import highest.flow.taobaolive.taobao.entity.TaobaoAccount;
import highest.flow.taobaolive.taobao.service.TaobaoAccountService;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service("taobaoAccountService")
public class TaobaoAccountServiceImpl extends ServiceImpl<TaobaoAccountDao, TaobaoAccount> implements TaobaoAccountService {

    @Override
    public TaobaoAccount register(String accountId, String nick, String sid, String utdid, String devid, String autoLoginToken, List<Cookie> cookies, int expires) {
        try {
            TaobaoAccount taobaoAccount = new TaobaoAccount();

            taobaoAccount.setAccountId(accountId);
            taobaoAccount.setNick(nick);
            taobaoAccount.setSid(sid);
            taobaoAccount.setUtdid(utdid);
            taobaoAccount.setDevid(devid);
            taobaoAccount.setAutoLoginToken(autoLoginToken);

            CookieStore cookieStore = new BasicCookieStore();
            for (Cookie cookie : cookies) {
                cookieStore.addCookie(cookie);
            }
            taobaoAccount.setCookieStore(cookieStore);

            Date expireDate = new Date();
            expireDate.setTime(expireDate.getTime() + expires);
            taobaoAccount.setExpires(expireDate);
            taobaoAccount.setState(TaobaoAccountState.Normal.getState());
            taobaoAccount.setCreatedTime(new Date());
            taobaoAccount.setUpdatedTime(new Date());

            this.save(taobaoAccount);
            return taobaoAccount;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public TaobaoAccount getInfo(String accountId) {
        return baseMapper.selectOne(Wrappers.<TaobaoAccount>lambdaQuery().eq(TaobaoAccount::getAccountId, accountId));
    }
}
