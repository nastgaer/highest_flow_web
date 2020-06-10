package highest.flow.taobaolive.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import highest.flow.taobaolive.common.defines.ErrorCodes;
import highest.flow.taobaolive.common.http.CookieHelper;
import highest.flow.taobaolive.common.utils.Query;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.service.AutoLoginService;
import highest.flow.taobaolive.taobao.dao.TaobaoAccountDao;
import highest.flow.taobaolive.taobao.defines.TaobaoAccountState;
import highest.flow.taobaolive.taobao.entity.TaobaoAccount;
import highest.flow.taobaolive.taobao.service.TaobaoAccountService;
import highest.flow.taobaolive.taobao.service.TaobaoApiService;
import org.apache.http.cookie.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

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
                R r = taobaoApiService.getUserSimple(taobaoAccount);
                if (r.getCode() == ErrorCodes.SUCCESS) {
                    // 正常
                } else if (r.getCode() == ErrorCodes.FAIL_SYS_SESSION_EXPIRED) {
                    r = taobaoApiService.autoLogin(taobaoAccount);

                    if (r.getCode() == ErrorCodes.SUCCESS) {
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
                        taobaoAccount.setExpires(new Date(expires));

                        List<String> cookieHeaders = new ArrayList<>();
                        for (Cookie cookie : lstCookies) {
                            cookieHeaders.add(CookieHelper.toString(cookie));
                        }
                        ObjectMapper objectMapper = new ObjectMapper();
                        taobaoAccount.setCookie(objectMapper.writeValueAsString(cookieHeaders));

                        taobaoAccount.setState(TaobaoAccountState.Normal.getState());
                    }

                    if (r.getCode() == ErrorCodes.SUCCESS) {
                        r = taobaoApiService.postpone(taobaoAccount);
                        if (r.getCode() == ErrorCodes.SUCCESS) {
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
                            taobaoAccount.setExpires(new Date(expires));

                            List<String> cookieHeaders = new ArrayList<>();
                            for (Cookie cookie : lstCookies) {
                                cookieHeaders.add(CookieHelper.toString(cookie));
                            }
                            ObjectMapper objectMapper = new ObjectMapper();
                            taobaoAccount.setCookie(objectMapper.writeValueAsString(cookieHeaders));

                            taobaoAccount.setState(TaobaoAccountState.Normal.getState());
                        }
                    }

                    if (r.getCode() != ErrorCodes.SUCCESS) {
                        taobaoAccount.setState(TaobaoAccountState.Expired.getState());
                    }
                }

                taobaoAccountService.save(taobaoAccount);

                Thread.sleep(100);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        logger.info("重登延期结束, 正常账号数：" + activeCount);
    }
}
