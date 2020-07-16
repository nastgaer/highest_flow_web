package highest.flow.taobaolive.task;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import highest.flow.taobaolive.common.config.Config;
import highest.flow.taobaolive.common.defines.ErrorCodes;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.job.entity.ScheduleJobEntity;
import highest.flow.taobaolive.job.utils.ScheduleUtils;
import highest.flow.taobaolive.taobao.defines.RankingEntityState;
import highest.flow.taobaolive.taobao.defines.RankingScore;
import highest.flow.taobaolive.taobao.defines.TaobaoAccountState;
import highest.flow.taobaolive.taobao.entity.LiveRoomEntity;
import highest.flow.taobaolive.taobao.entity.ProductEntity;
import highest.flow.taobaolive.taobao.entity.RankingEntity;
import highest.flow.taobaolive.taobao.entity.TaobaoAccountEntity;
import highest.flow.taobaolive.taobao.service.RankingService;
import highest.flow.taobaolive.taobao.service.TaobaoAccountService;
import highest.flow.taobaolive.taobao.service.TaobaoApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

@Component("assistRankingTask")
@Scope("prototype")  // 不会Singleton
public class AssistRankingTask implements ITask {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${taobaolive.simulate:false}")
    private boolean simulate;

    @Autowired
    private TaobaoApiService taobaoApiService;

    @Autowired
    private TaobaoAccountService taobaoAccountService;

    @Autowired
    private RankingService rankingService;

    private CountDownLatch countDownLatch = null;

    private Thread monitorThread = null;

    private ScheduleJobEntity scheduleJobEntity = null;

    @Autowired
    private Executor rankingExecutor;

    @Override
    public void run(ScheduleJobEntity scheduleJobEntity) {
        String params = scheduleJobEntity.getParams();
        logger.info("打助力开始, 参数=" + params);

        RankingEntity rankingEntity = null;

        this.scheduleJobEntity = scheduleJobEntity;

        try {
            int taskId = Integer.parseInt(params);

            rankingEntity = rankingService.getById(taskId);
            rankingEntity.setStartTime(new Date());
            rankingEntity.setState(RankingEntityState.Running.getState());

            List<TaobaoAccountEntity> taobaoAccountEntities = taobaoAccountService.list(Wrappers.<TaobaoAccountEntity>lambdaQuery().eq(TaobaoAccountEntity::getState, TaobaoAccountState.Normal.getState()));
            if (taobaoAccountEntities.size() < 1) {
                rankingEntity.setState(RankingEntityState.Stopped.getState());
                return;
            }

            LiveRoomEntity liveRoomEntity = null;

            TaobaoAccountEntity activeAccount = taobaoAccountEntities.get(0);
            R r = taobaoApiService.getLiveInfo(rankingEntity.getTaocode(), activeAccount);
            if (r.getCode() == ErrorCodes.SUCCESS) {
                liveRoomEntity = (LiveRoomEntity) r.get("live_room");
                if (liveRoomEntity != null) {
                    taobaoApiService.getLiveProducts(liveRoomEntity, activeAccount);
                }
            }

            if (simulate) {
                if (liveRoomEntity == null) {
                    rankingEntity.setState(RankingEntityState.Stopped.getState());
                    return;
                }
            } else {
                if (liveRoomEntity == null || !liveRoomEntity.isHasRankingEntry()) {
                    rankingEntity.setState(RankingEntityState.Stopped.getState());
                    return;
                }
            }

            rankingEntity.setUpdatedTime(new Date());
            rankingService.updateById(rankingEntity);

            monitorThread = new Thread(new MonitorWorker(rankingEntity, liveRoomEntity, taobaoAccountEntities));
            monitorThread.start();

            int unitScore = rankingEntity.isDoubleBuy() ?
                    (RankingScore.DoubleBuyFollow.getScore() + RankingScore.DoubleBuyBuy.getScore() + RankingScore.DoubleBuyWatch.getScore()) :
                    (RankingScore.Follow.getScore() + RankingScore.Buy.getScore() + RankingScore.Watch.getScore());

            int leftScore = rankingEntity.getTargetScore();
            int startIndex = 0, endScore = -1;

            // 直到达到目标
            while (leftScore >= 0 && this.isRunning(rankingEntity)) {
                int totalCount = leftScore / unitScore + 1;

                if (startIndex + totalCount >= taobaoAccountEntities.size()) {
                    break;
                }

                countDownLatch = new CountDownLatch(totalCount);

                int assistCount = 0;
                for (int idx = startIndex; idx < startIndex + totalCount && idx < taobaoAccountEntities.size() && this.isRunning(rankingEntity); idx++) {
                    TaobaoAccountEntity taobaoAccountEntity = taobaoAccountEntities.get(idx);
                    rankingExecutor.execute(
                            new AssistRunnable(rankingEntity, liveRoomEntity, taobaoAccountEntity)
                    );
                    assistCount++;
                }

                countDownLatch.await();

                startIndex += totalCount;

                endScore = -1;
                if (simulate) {
                    endScore = liveRoomEntity.getRankingListData().getRankingScore();

                } else {
                    for (int retry = 0; retry < Config.MAX_RETRY; retry++) {
                        activeAccount = taobaoAccountEntities.get(retry);
                        r = taobaoApiService.getLiveEntry(liveRoomEntity, activeAccount);
                        if (r.getCode() == ErrorCodes.SUCCESS) {
                            endScore = liveRoomEntity.getRankingListData().getRankingScore();
                            break;
                        }
                    }
                }

                leftScore = rankingEntity.getTargetScore() - (endScore - rankingEntity.getStartScore());
                if (endScore < 0) {
                    break;
                }
            }

            rankingEntity.setEndScore(endScore);
            if (leftScore > 0) {
                rankingEntity.setState(RankingEntityState.Stopped.getState());
            } else {
                rankingEntity.setState(RankingEntityState.Finished.getState());
            }

            rankingEntity.setEndTime(new Date());
            rankingEntity.setUpdatedTime(new Date());
            rankingService.updateById(rankingEntity);

            monitorThread.join();

            logger.info("MonitorThread 结束");

        } catch (Exception ex) {
            ex.printStackTrace();

            rankingEntity.setState(RankingEntityState.Error.getState());

        } finally {
            if (rankingEntity != null) {
                rankingEntity.setEndTime(new Date());
                rankingEntity.setUpdatedTime(new Date());
                rankingService.updateById(rankingEntity);
            }

            rankingService.deleteTask(rankingEntity);

            logger.info("打助力结束");
        }
    }

    private boolean isRunning(RankingEntity rankingEntity) {
        return rankingService.isRunning(rankingEntity, scheduleJobEntity.getId());
    }

    class AssistRunnable implements Runnable {

        private RankingEntity rankingEntity;
        private LiveRoomEntity liveRoomEntity;
        private TaobaoAccountEntity taobaoAccountEntity;

        public AssistRunnable(RankingEntity rankingEntity, LiveRoomEntity liveRoomEntity, TaobaoAccountEntity activeAccount) {
            this.rankingEntity = rankingEntity;
            this.liveRoomEntity = liveRoomEntity;
            this.taobaoAccountEntity = activeAccount;
        }

        @Override
        public void run() {
            assistRanking(rankingEntity,
                    liveRoomEntity,
                    taobaoAccountEntity);
        }

        private void simulateAssistRanking(RankingEntity rankingEntity, LiveRoomEntity liveRoomEntity, TaobaoAccountEntity activeAccount) {
            try {
                // 关注
                {
                    Thread.sleep(300);
                    int followScore = rankingEntity.isDoubleBuy() ? RankingScore.DoubleBuyFollow.getScore() : RankingScore.Follow.getScore();
                    liveRoomEntity.getRankingListData().setRankingScore(liveRoomEntity.getRankingListData().getRankingScore() + followScore);
                }

                if (!isRunning(rankingEntity)) {
                    return;
                }

                // 观看停留
                {
                    int stayScore = rankingEntity.isDoubleBuy() ? RankingScore.DoubleBuyWatch.getScore() : RankingScore.Watch.getScore();
                    for (int time = 60; time <= 3000; time += 60) {
                        Thread.sleep(300);

                        if (!isRunning(rankingEntity)) {
                            return;
                        }
                    }
                    liveRoomEntity.getRankingListData().setRankingScore(liveRoomEntity.getRankingListData().getRankingScore() + stayScore);
                }

                if (!isRunning(rankingEntity)) {
                    return;
                }

                // 购买
                {
                    int buyScore = rankingEntity.isDoubleBuy() ? RankingScore.DoubleBuyBuy.getScore() : RankingScore.Buy.getScore();
                    List<ProductEntity> productEntities = liveRoomEntity.getProducts();
                    for (int idx = 0; idx < productEntities.size(); idx++) {
                        Thread.sleep(300);

                        if (!isRunning(rankingEntity)) {
                            return;
                        }
                    }
                    liveRoomEntity.getRankingListData().setRankingScore(liveRoomEntity.getRankingListData().getRankingScore() + buyScore);
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                countDownLatch.countDown();
            }
        }

        private void assistRanking(RankingEntity rankingEntity, LiveRoomEntity liveRoomEntity, TaobaoAccountEntity activeAccount) {
            if (simulate) {
                simulateAssistRanking(rankingEntity, liveRoomEntity, activeAccount);
                return;
            }

            try {
                // 初始化
                taobaoApiService.getUserSimple(activeAccount);

                if (!isRunning(rankingEntity)) {
                    return;
                }

                // 关注
                {
                    R r = taobaoApiService.taskFollow(liveRoomEntity, activeAccount);
                    if (r.getCode() != ErrorCodes.SUCCESS) {
                        r = taobaoApiService.taskFollow(liveRoomEntity, activeAccount);
                    }
                    if (r.getCode() != ErrorCodes.SUCCESS) {
                        r = taobaoApiService.taskFollow(liveRoomEntity, activeAccount);
                    }
                }

                if (!isRunning(rankingEntity)) {
                    return;
                }

                {
                    // 观看停留
                    for (int time = 60; time <= 3000; time += 60) {
                        R r = taobaoApiService.taskStay(liveRoomEntity, activeAccount, time);

                        if (r.getCode() != ErrorCodes.SUCCESS) {
                            r = taobaoApiService.taskStay(liveRoomEntity, activeAccount, time);
                        }
                        if (r.getCode() != ErrorCodes.SUCCESS) {
                            r = taobaoApiService.taskStay(liveRoomEntity, activeAccount, time);
                        }

                        if (!isRunning(rankingEntity)) {
                            return;
                        }
                    }
                }

                if (!isRunning(rankingEntity)) {
                    return;
                }

                {
                    // 购买
                    List<ProductEntity> productEntities = liveRoomEntity.getProducts();
                    for (int idx = 0; idx < productEntities.size(); idx++) {
                        R r = taobaoApiService.taskBuy(liveRoomEntity, activeAccount, productEntities.get(idx).getProductId());
                        if (r.getCode() != ErrorCodes.SUCCESS) {
                            r = taobaoApiService.taskBuy(liveRoomEntity, activeAccount, productEntities.get(idx).getProductId());
                        }
                        if (r.getCode() != ErrorCodes.SUCCESS) {
                            r = taobaoApiService.taskBuy(liveRoomEntity, activeAccount, productEntities.get(idx).getProductId());
                        }

                        if (!isRunning(rankingEntity)) {
                            return;
                        }
                    }
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                countDownLatch.countDown();
            }
        }
    }

    // 隔一秒更新一次
    class MonitorWorker implements Runnable {

        private RankingEntity rankingEntity;
        private LiveRoomEntity liveRoomEntity;
        private List<TaobaoAccountEntity> taobaoAccountEntities;

        public MonitorWorker(RankingEntity rankingEntity, LiveRoomEntity liveRoomEntity, List<TaobaoAccountEntity> taobaoAccountEntities) {
            this.rankingEntity = rankingEntity;
            this.liveRoomEntity = liveRoomEntity;
            this.taobaoAccountEntities = taobaoAccountEntities;
        }

        @Override
        public void run() {
            while (rankingEntity.getState() == RankingEntityState.Running.getState()) {
                try {
                    if (simulate) {
                        int currentScore = liveRoomEntity.getRankingListData().getRankingScore();
                        rankingEntity.setEndScore(currentScore);
                        rankingService.updateById(rankingEntity);
                    } else {
                        for (int retry = 0; retry < Config.MAX_RETRY; retry++) {
                            TaobaoAccountEntity activeAccount = taobaoAccountEntities.get(retry);
                            R r = taobaoApiService.getLiveEntry(liveRoomEntity, activeAccount);
                            if (r.getCode() == ErrorCodes.SUCCESS) {
                                int currentScore = liveRoomEntity.getRankingListData().getRankingScore();

                                rankingEntity.setEndScore(currentScore);
                                rankingService.updateById(rankingEntity);
                                break;
                            }
                        }
                    }

                    Thread.sleep(1000);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
