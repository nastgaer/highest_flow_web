package highest.flow.taobaolive.task;

import highest.flow.taobaolive.common.defines.ErrorCodes;
import highest.flow.taobaolive.common.utils.CommonUtils;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.job.entity.ScheduleJobEntity;
import highest.flow.taobaolive.taobao.defines.TaobaoAccountLogKind;
import highest.flow.taobaolive.taobao.defines.TaobaoAccountState;
import highest.flow.taobaolive.taobao.entity.TaobaoAccountEntity;
import highest.flow.taobaolive.taobao.entity.TaobaoAccountLogEntity;
import highest.flow.taobaolive.taobao.service.TaobaoAccountLogService;
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

    @Autowired
    private TaobaoAccountLogService taobaoAccountLogService;

    @Override
    public void run(ScheduleJobEntity scheduleJobEntity) {
        String params = scheduleJobEntity.getParams();
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

                    } else {
                        logger.error("[" + taobaoAccountEntity.getNick() + "] 用户延期失败：" + r.getMsg());
                    }

                    TaobaoAccountLogEntity taobaoAccountLogEntity = new TaobaoAccountLogEntity();
                    taobaoAccountLogEntity.setKind(TaobaoAccountLogKind.Postpone.getKind());
                    taobaoAccountLogEntity.setUid(taobaoAccountEntity.getUid());
                    taobaoAccountLogEntity.setNick(taobaoAccountEntity.getNick());
                    taobaoAccountLogEntity.setSuccess(r.getCode() == ErrorCodes.SUCCESS ? 1 : 0);
                    taobaoAccountLogEntity.setExpires(taobaoAccountEntity.getExpires());
                    taobaoAccountLogEntity.setContent(r.getMsg());
                    taobaoAccountLogEntity.setCreatedTime(new Date());
                    taobaoAccountLogService.save(taobaoAccountLogEntity);
                }

                if (r.getCode() != ErrorCodes.SUCCESS) {
                    logger.info("[" + taobaoAccountEntity.getNick() + "] 用户开始重登");
                    r = taobaoApiService.autoLogin(taobaoAccountEntity);

                    if (r.getCode() == ErrorCodes.SUCCESS) {
                        logger.info("[" + taobaoAccountEntity.getNick() + "] 用户重登成功");

                    } else {
                        logger.error("[" + taobaoAccountEntity.getNick() + "] 用户重登失败：" + r.getMsg());
                    }

                    TaobaoAccountLogEntity taobaoAccountLogEntity = new TaobaoAccountLogEntity();
                    taobaoAccountLogEntity.setKind(TaobaoAccountLogKind.AutoLogin.getKind());
                    taobaoAccountLogEntity.setUid(taobaoAccountEntity.getUid());
                    taobaoAccountLogEntity.setNick(taobaoAccountEntity.getNick());
                    taobaoAccountLogEntity.setSuccess(r.getCode() == ErrorCodes.SUCCESS ? 1 : 0);
                    taobaoAccountLogEntity.setExpires(taobaoAccountEntity.getExpires());
                    taobaoAccountLogEntity.setContent(r.getMsg());
                    taobaoAccountLogEntity.setCreatedTime(new Date());
                    taobaoAccountLogService.save(taobaoAccountLogEntity);
                }

                if (taobaoAccountEntity.getState() == TaobaoAccountState.Normal.getState()) {
                    activeCount++;
                }

                taobaoAccountService.updateById(taobaoAccountEntity);

                Thread.sleep(100);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        logger.info("重登延期结束, 正常账号数：" + activeCount);
    }
}
