package highest.flow.taobaolive.taobao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import highest.flow.taobaolive.common.defines.ErrorCodes;
import highest.flow.taobaolive.common.utils.CommonUtils;
import highest.flow.taobaolive.common.utils.Query;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.taobao.dao.TaobaoAccountDao;
import highest.flow.taobaolive.taobao.defines.TaobaoAccountState;
import highest.flow.taobaolive.taobao.entity.TaobaoAccount;
import highest.flow.taobaolive.taobao.service.AutoLoginService;
import highest.flow.taobaolive.taobao.service.TaobaoAccountService;
import highest.flow.taobaolive.taobao.service.TaobaoApiService;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("autoLoginService")
public class AutoLoginServiceImpl extends ServiceImpl<TaobaoAccountDao, TaobaoAccount> implements AutoLoginService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private TaobaoApiService taobaoApiService;

    @Autowired
    private TaobaoAccountService taobaoAccountService;

    @Override
    public void doAutoLogin() {
        QueryWrapper<TaobaoAccount> queryWrapper = new QueryWrapper<>();
        Map<String, Object> pageMap = new HashMap<String, Object>();
        IPage<TaobaoAccount> pageResult = this.page(new Query<TaobaoAccount>().getPage(pageMap), queryWrapper);
        List<TaobaoAccount> taobaoAccounts = pageResult.getRecords();

        logger.info("重登延期开始, accountCount=" + taobaoAccounts.size());

        int activeCount = 0;
        for (TaobaoAccount taobaoAccount : taobaoAccounts) {
            try {
                logger.info("[" + taobaoAccount.getNick() + "] 用户开始延期+重登");

                R r = taobaoApiService.getUserSimple(taobaoAccount);
                if (r.getCode() == ErrorCodes.FAIL_SYS_SESSION_EXPIRED) {

                } else {
                    // 正常
                    logger.info("[" + taobaoAccount.getNick() + "] 用户开始延期");
                    r = taobaoApiService.postpone(taobaoAccount);
                    if (r.getCode() == ErrorCodes.SUCCESS) {
                        logger.info("[" + taobaoAccount.getNick() + "] 用户延期成功");

                        long expires = (long) r.get("expires");
                        String autoLoginToken = (String) r.get("autoLoginToken");
                        List<Cookie> lstCookies = (List<Cookie>) r.get("cookie");
                        String sid = (String) r.get("sid");
                        String uid = (String) r.get("uid");
                        String nick = (String) r.get("nick");

                        taobaoAccount.setAutoLoginToken(autoLoginToken);
                        taobaoAccount.setSid(sid);
                        taobaoAccount.setUid(uid);
                        taobaoAccount.setNick(nick);
                        taobaoAccount.setExpires(CommonUtils.timestampToDate(expires * 1000));

                        CookieStore cookieStore = new BasicCookieStore();
                        for (Cookie cookie : lstCookies) {
                            cookieStore.addCookie(cookie);
                        }
                        taobaoAccount.setCookieStore(cookieStore);

                        taobaoAccount.setState(TaobaoAccountState.Normal.getState());

                    } else {
                        logger.error("[" + taobaoAccount.getNick() + "] 用户延期失败：" + r.getMsg());
                        taobaoAccount.setState(TaobaoAccountState.Expired.getState());
                    }
                }

                if (r.getCode() != ErrorCodes.SUCCESS) {
                    logger.info("[" + taobaoAccount.getNick() + "] 用户开始重登");
                    r = taobaoApiService.autoLogin(taobaoAccount);

                    if (r.getCode() == ErrorCodes.SUCCESS) {
                        logger.info("[" + taobaoAccount.getNick() + "] 用户重登成功");

                        long expires = (long) r.get("expires");
                        String autoLoginToken = (String) r.get("autoLoginToken");
                        List<Cookie> lstCookies = (List<Cookie>) r.get("cookie");
                        String sid = (String) r.get("sid");
                        String uid = (String) r.get("uid");
                        String nick = (String) r.get("nick");

                        taobaoAccount.setAutoLoginToken(autoLoginToken);
                        taobaoAccount.setSid(sid);
                        taobaoAccount.setUid(uid);
                        taobaoAccount.setNick(nick);
                        taobaoAccount.setExpires(CommonUtils.timestampToDate(expires * 1000));

                        CookieStore cookieStore = new BasicCookieStore();
                        for (Cookie cookie : lstCookies) {
                            cookieStore.addCookie(cookie);
                        }
                        taobaoAccount.setCookieStore(cookieStore);

                        taobaoAccount.setState(TaobaoAccountState.Normal.getState());
                    } else {
                        logger.error("[" + taobaoAccount.getNick() + "] 用户重登失败：" + r.getMsg());
                        taobaoAccount.setState(TaobaoAccountState.Expired.getState());
                    }
                }

                if (taobaoAccount.getState() == TaobaoAccountState.Normal.getState()) {
                    activeCount++;
                }

                taobaoAccount.setUpdatedTime(new Date());
                taobaoAccountService.updateById(taobaoAccount);

                Thread.sleep(100);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        logger.info("重登延期结束, 正常账号数：" + activeCount);
    }
}
