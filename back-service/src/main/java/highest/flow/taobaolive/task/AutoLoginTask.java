package highest.flow.taobaolive.task;

import highest.flow.taobaolive.common.defines.ErrorCodes;
import highest.flow.taobaolive.common.utils.CommonUtils;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.taobao.defines.TaobaoAccountState;
import highest.flow.taobaolive.taobao.entity.TaobaoAccountEntity;
import highest.flow.taobaolive.taobao.service.TaobaoAccountService;
import highest.flow.taobaolive.taobao.service.TaobaoApiService;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component("autoLoginTask")
public class AutoLoginTask implements ITask {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private TaobaoApiService taobaoApiService;

    @Autowired
    private TaobaoAccountService taobaoAccountService;

    @Override
    public void run(String params) {
        List<TaobaoAccountEntity> taobaoAccountEntities = taobaoAccountService.list();

        logger.info("重登延期开始, accountCount=" + taobaoAccountEntities.size());

        int activeCount = 0;
        for (TaobaoAccountEntity taobaoAccountEntity : taobaoAccountEntities) {
            try {
                logger.info("[" + taobaoAccountEntity.getNick() + "] 用户开始延期+重登");

                R r = taobaoApiService.getUserSimple(taobaoAccountEntity);
                if (r.getCode() == ErrorCodes.FAIL_SYS_SESSION_EXPIRED) {

                } else {
                    // 正常
                    logger.info("[" + taobaoAccountEntity.getNick() + "] 用户开始延期");
                    r = taobaoApiService.postpone(taobaoAccountEntity);
                    if (r.getCode() == ErrorCodes.SUCCESS) {
                        logger.info("[" + taobaoAccountEntity.getNick() + "] 用户延期成功");

                        long expires = (long) r.get("expires");
                        String autoLoginToken = (String) r.get("autoLoginToken");
                        List<Cookie> lstCookies = (List<Cookie>) r.get("cookie");
                        String sid = (String) r.get("sid");
                        String uid = (String) r.get("uid");
                        String nick = (String) r.get("nick");

                        taobaoAccountEntity.setAutoLoginToken(autoLoginToken);
                        taobaoAccountEntity.setSid(sid);
                        taobaoAccountEntity.setUid(uid);
                        taobaoAccountEntity.setNick(nick);
                        taobaoAccountEntity.setExpires(CommonUtils.timestampToDate(expires * 1000));

                        CookieStore cookieStore = new BasicCookieStore();
                        for (Cookie cookie : lstCookies) {
                            cookieStore.addCookie(cookie);
                        }
                        taobaoAccountEntity.setCookieStore(cookieStore);

                        taobaoAccountEntity.setState(TaobaoAccountState.Normal.getState());

                    } else {
                        logger.error("[" + taobaoAccountEntity.getNick() + "] 用户延期失败：" + r.getMsg());
                        taobaoAccountEntity.setState(TaobaoAccountState.Expired.getState());
                    }
                }

                if (r.getCode() != ErrorCodes.SUCCESS) {
                    logger.info("[" + taobaoAccountEntity.getNick() + "] 用户开始重登");
                    r = taobaoApiService.autoLogin(taobaoAccountEntity);

                    if (r.getCode() == ErrorCodes.SUCCESS) {
                        logger.info("[" + taobaoAccountEntity.getNick() + "] 用户重登成功");

                        long expires = (long) r.get("expires");
                        String autoLoginToken = (String) r.get("autoLoginToken");
                        List<Cookie> lstCookies = (List<Cookie>) r.get("cookie");
                        String sid = (String) r.get("sid");
                        String uid = (String) r.get("uid");
                        String nick = (String) r.get("nick");

                        taobaoAccountEntity.setAutoLoginToken(autoLoginToken);
                        taobaoAccountEntity.setSid(sid);
                        taobaoAccountEntity.setUid(uid);
                        taobaoAccountEntity.setNick(nick);
                        taobaoAccountEntity.setExpires(CommonUtils.timestampToDate(expires * 1000));

                        CookieStore cookieStore = new BasicCookieStore();
                        for (Cookie cookie : lstCookies) {
                            cookieStore.addCookie(cookie);
                        }
                        taobaoAccountEntity.setCookieStore(cookieStore);

                        taobaoAccountEntity.setState(TaobaoAccountState.Normal.getState());

                    } else {
                        logger.error("[" + taobaoAccountEntity.getNick() + "] 用户重登失败：" + r.getMsg());
                        taobaoAccountEntity.setState(TaobaoAccountState.Expired.getState());
                    }
                }

                if (taobaoAccountEntity.getState() == TaobaoAccountState.Normal.getState()) {
                    activeCount++;
                }

                taobaoAccountEntity.setUpdatedTime(new Date());
                taobaoAccountService.updateById(taobaoAccountEntity);

                Thread.sleep(100);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        logger.info("重登延期结束, 正常账号数：" + activeCount);
    }
}
