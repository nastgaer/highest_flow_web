package highest.flow.taobaolive.task;

import highest.flow.taobaolive.common.defines.ErrorCodes;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.job.entity.ScheduleJobEntity;
import highest.flow.taobaolive.taobao.defines.TaobaoAccountLogKind;
import highest.flow.taobaolive.taobao.defines.TaobaoAccountState;
import highest.flow.taobaolive.taobao.entity.TaobaoAccountEntity;
import highest.flow.taobaolive.taobao.entity.TaobaoAccountLogEntity;
import highest.flow.taobaolive.taobao.service.TaobaoAccountLogService;
import highest.flow.taobaolive.taobao.service.TaobaoAccountService;
import highest.flow.taobaolive.taobao.service.TaobaoApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

@Component("autoLoginTask")
public class AutoLoginTask implements ITask {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private TaobaoApiService taobaoApiService;

    @Autowired
    private TaobaoAccountService taobaoAccountService;

    @Autowired
    private TaobaoAccountLogService taobaoAccountLogService;

    @Value("${autologin.thread-count:10}")
    private int threadCount;

    private CountDownLatch countDownLatch = null;

    @Override
    public void run(ScheduleJobEntity scheduleJobEntity) {
        String params = scheduleJobEntity == null ? "" : scheduleJobEntity.getParams();

        try {
            int totalCount = this.taobaoAccountService.count();
            logger.info("重登延期开始, accountCount=" + totalCount);

            countDownLatch = new CountDownLatch(threadCount);

            int countPerThread = totalCount / threadCount + 1;

            List<TaobaoAccountEntity> taobaoAccountEntities = taobaoAccountService.list();

            for (int idx = 0; idx < Math.min(threadCount, totalCount); idx++) {
                int threadIndex = idx;
                try {
                    Thread thread = new Thread(new AutoLoginRunnable(taobaoAccountEntities, threadIndex, countPerThread));
                    thread.start();

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            countDownLatch.await();

            logger.info("重登延期结束");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    class AutoLoginRunnable implements Runnable {

        private List<TaobaoAccountEntity> taobaoAccountEntities = null;

        private int threadIndex = 0;

        private int countPerThread = 0;

        public AutoLoginRunnable(List<TaobaoAccountEntity> taobaoAccountEntities, int threadIndex, int countPerThread) {
            this.taobaoAccountEntities = taobaoAccountEntities;
            this.threadIndex = threadIndex;
            this.countPerThread = countPerThread;
        }

        @Override
        public void run() {
            if (taobaoAccountEntities == null) {
                return;
            }

            int activeCount = 0;
            int startIndex = threadIndex * countPerThread;
            int endIndex = Math.min(taobaoAccountEntities.size(), startIndex + countPerThread);

            logger.info("[" + threadIndex + "]重登延期任务开始了, " + startIndex + " ~  " + endIndex);

            for (int idx = startIndex; idx < endIndex; idx++) {
                try {
                    TaobaoAccountEntity taobaoAccountEntity = taobaoAccountEntities.get(idx);

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
                        taobaoAccountLogEntity.setMemberId(taobaoAccountEntity.getMemberId());
                        taobaoAccountLogEntity.setKind(TaobaoAccountLogKind.Postpone.getKind());
                        taobaoAccountLogEntity.setUid(taobaoAccountEntity.getUid());
                        taobaoAccountLogEntity.setNick(taobaoAccountEntity.getNick());
                        taobaoAccountLogEntity.setSuccess(r.getCode() == ErrorCodes.SUCCESS ? 1 : 0);
                        taobaoAccountLogEntity.setExpires(taobaoAccountEntity.getExpires());
                        taobaoAccountLogEntity.setContent(r.getMsg());
                        taobaoAccountLogEntity.setCreatedTime(new Date());
                        taobaoAccountLogService.save(taobaoAccountLogEntity);

                        taobaoAccountService.cacheAccount(taobaoAccountEntity);
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
                        taobaoAccountLogEntity.setMemberId(taobaoAccountEntity.getMemberId());
                        taobaoAccountLogEntity.setKind(TaobaoAccountLogKind.AutoLogin.getKind());
                        taobaoAccountLogEntity.setUid(taobaoAccountEntity.getUid());
                        taobaoAccountLogEntity.setNick(taobaoAccountEntity.getNick());
                        taobaoAccountLogEntity.setSuccess(r.getCode() == ErrorCodes.SUCCESS ? 1 : 0);
                        taobaoAccountLogEntity.setExpires(taobaoAccountEntity.getExpires());
                        taobaoAccountLogEntity.setContent(r.getMsg());
                        taobaoAccountLogEntity.setCreatedTime(new Date());
                        taobaoAccountLogService.save(taobaoAccountLogEntity);

                        taobaoAccountService.cacheAccount(taobaoAccountEntity);
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

            try {
                countDownLatch.countDown();

            } catch (Exception ex) {
                ex.printStackTrace();
            }

            logger.info("[" + threadIndex + "]重登延期任务结束了, 正常小号数：" + activeCount);
        }
    }
}
