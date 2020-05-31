package highest.flow.taobaolive.taobao.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import highest.flow.taobaolive.taobao.dao.TaobaoAccountDao;
import highest.flow.taobaolive.taobao.defines.TaobaoAccountState;
import highest.flow.taobaolive.taobao.entity.TaobaoAccount;
import highest.flow.taobaolive.taobao.service.TaobaoAccountService;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;

@Service("taobaoAccountService")
public class TaobaoAccountServiceImpl extends ServiceImpl<TaobaoAccountDao, TaobaoAccount> implements TaobaoAccountService {

    @Override
    public TaobaoAccount register(String accountId, String nick, String sid, String utdid, String devid, String autoLoginToken, String cookie, int expires) {
        TaobaoAccount taobaoAccount = new TaobaoAccount();

        taobaoAccount.setAccountId(accountId);
        taobaoAccount.setNick(nick);
        taobaoAccount.setSid(sid);
        taobaoAccount.setUtdid(utdid);
        taobaoAccount.setDevid(devid);
        taobaoAccount.setAutoLoginToken(autoLoginToken);
        taobaoAccount.setCookie(cookie);

        Date expireDate = new Date();
        expireDate.setTime(expireDate.getTime() + expires);
        taobaoAccount.setExpires(expireDate);
        taobaoAccount.setState(TaobaoAccountState.Normal.getState());
        taobaoAccount.setCreatedTime(new Date());
        taobaoAccount.setUpdatedTime(new Date());

        this.save(taobaoAccount);
        return taobaoAccount;
    }

    @Override
    public TaobaoAccount getInfo(String accountId) {
        return baseMapper.selectOne(Wrappers.<TaobaoAccount>lambdaQuery().eq(TaobaoAccount::getAccountId, accountId));
    }
}
