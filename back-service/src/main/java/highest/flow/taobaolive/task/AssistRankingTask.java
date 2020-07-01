package highest.flow.taobaolive.task;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import highest.flow.taobaolive.common.config.Config;
import highest.flow.taobaolive.common.defines.ErrorCodes;
import highest.flow.taobaolive.common.utils.R;
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
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@Component("assistRankingTask")
@Scope("prototype")
public class AssistRankingTask implements ITask {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private TaobaoApiService taobaoApiService;

    @Autowired
    private TaobaoAccountService taobaoAccountService;

    @Autowired
    private RankingService rankingService;

    private CountDownLatch countDownLatch = null;

    private Thread monitorThread = null;

    @Override
    public void run(String params) {
        logger.info("打助力开始, 参数=" + params);

        try {
            int taskId = Integer.parseInt(params);

            RankingEntity rankingEntity = rankingService.getById(taskId);

            List<TaobaoAccountEntity> taobaoAccountEntities = taobaoAccountService.list(Wrappers.<TaobaoAccountEntity>lambdaQuery().eq(TaobaoAccountEntity::getState, TaobaoAccountState.Normal.getState()));

            LiveRoomEntity liveRoomEntity = null;
            for (int retry = 0; retry < Config.MAX_RETRY; retry++) {
                TaobaoAccountEntity activeAccount = taobaoAccountEntities.get(retry);
                R r = taobaoApiService.getLiveEntry(liveRoomEntity, activeAccount);
                if (r.getCode() == ErrorCodes.SUCCESS) {
                    r = taobaoApiService.getLiveProducts(liveRoomEntity, activeAccount);
                    break;
                }
            }

            if (liveRoomEntity == null) {
                return;
            }

            rankingEntity.setStartTime(new Date());
            rankingEntity.setUpdateTime(new Date());
            rankingEntity.setState(RankingEntityState.Running);
            rankingService.updateById(rankingEntity);

            monitorThread = new Thread(new MonitorWorker(rankingEntity, liveRoomEntity, taobaoAccountEntities));
            monitorThread.start();

            int unitScore = rankingEntity.isDoubleBuy() ?
                    (RankingScore.DoubleBuyFollow.getScore() + RankingScore.DoubleBuyBuy.getScore() + RankingScore.DoubleBuyWatch.getScore()) :
                    (RankingScore.Follow.getScore() + RankingScore.Buy.getScore() + RankingScore.Watch.getScore());

            int leftScore = rankingEntity.getTargetScore();
            int startIndex = 0, endScore = -1;

            // 直到达到目标
            while (leftScore >= 0) {
                int totalCount = leftScore / unitScore + 1;

                if (startIndex + totalCount >= taobaoAccountEntities.size()) {
                    break;
                }

                countDownLatch = new CountDownLatch(totalCount);

                for (int idx = startIndex; idx < startIndex + totalCount && idx < taobaoAccountEntities.size(); idx++) {
                    assistRanking(rankingEntity, liveRoomEntity, taobaoAccountEntities.get(idx));
                }

                countDownLatch.wait();

                startIndex += totalCount;

                endScore = -1;
                for (int retry = 0; retry < Config.MAX_RETRY; retry++) {
                    TaobaoAccountEntity activeAccount = taobaoAccountEntities.get(retry);
                    R r = taobaoApiService.getLiveEntry(liveRoomEntity, activeAccount);
                    if (r.getCode() == ErrorCodes.SUCCESS) {
                        endScore = liveRoomEntity.getRankingScore();
                        break;
                    }
                }
                if (endScore < 0) {
                    break;
                }

                leftScore = rankingEntity.getTargetScore() - (endScore - rankingEntity.getStartScore());
            }

            rankingEntity.setEndScore(endScore);
            if (endScore < 0) {
                rankingEntity.setState(RankingEntityState.Error);
            } else {
                rankingEntity.setState(RankingEntityState.Finished);
            }

            rankingEntity.setEndTime(new Date());
            rankingEntity.setUpdateTime(new Date());
            rankingService.updateById(rankingEntity);

            monitorThread.join();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        logger.info("打助力结束");
    }

    @Async("rankingThreadPool")
    public void assistRanking(RankingEntity rankingEntity, LiveRoomEntity liveRoomEntity, TaobaoAccountEntity activeAccount) {
        try {
            countDownLatch.countDown();

            taobaoApiService.getUserSimple(activeAccount);

            // 关注
            R r = taobaoApiService.taskFollow(liveRoomEntity, activeAccount);
            if (r.getCode() != ErrorCodes.SUCCESS) {
                r = taobaoApiService.taskFollow(liveRoomEntity, activeAccount);
            }
            if (r.getCode() != ErrorCodes.SUCCESS) {
                r = taobaoApiService.taskFollow(liveRoomEntity, activeAccount);
            }

            // 观看停留
            for (int time = 60; time <= 3000; time += 60) {
                r = taobaoApiService.taskStay(liveRoomEntity, activeAccount, time);

                if (r.getCode() != ErrorCodes.SUCCESS) {
                    r = taobaoApiService.taskStay(liveRoomEntity, activeAccount, time);
                }
                if (r.getCode() != ErrorCodes.SUCCESS) {
                    r = taobaoApiService.taskStay(liveRoomEntity, activeAccount, time);
                }
            }

            // 购买
            List<ProductEntity> productEntities = liveRoomEntity.getProducts();
            for (int idx = 0; idx < productEntities.size(); idx++) {
                r = taobaoApiService.taskBuy(liveRoomEntity, activeAccount, productEntities.get(idx).getProductId());
                if (r.getCode() != ErrorCodes.SUCCESS) {
                    r = taobaoApiService.taskBuy(liveRoomEntity, activeAccount, productEntities.get(idx).getProductId());
                }
                if (r.getCode() != ErrorCodes.SUCCESS) {
                    r = taobaoApiService.taskBuy(liveRoomEntity, activeAccount, productEntities.get(idx).getProductId());
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
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
                    for (int retry = 0; retry < Config.MAX_RETRY; retry++) {
                        TaobaoAccountEntity activeAccount = taobaoAccountEntities.get(retry);
                        R r = taobaoApiService.getLiveEntry(liveRoomEntity, activeAccount);
                        if (r.getCode() == ErrorCodes.SUCCESS) {
                            int currentScore = liveRoomEntity.getRankingScore();

                            rankingEntity.setEndScore(currentScore);
                            rankingService.updateById(rankingEntity);
                            break;
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
