package highest.flow.taobaolive.task;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import highest.flow.taobaolive.common.config.Config;
import highest.flow.taobaolive.common.defines.ErrorCodes;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.job.defines.ScheduleState;
import highest.flow.taobaolive.job.entity.ScheduleJobEntity;
import highest.flow.taobaolive.job.utils.ScheduleUtils;
import highest.flow.taobaolive.sys.entity.SysMember;
import highest.flow.taobaolive.sys.service.MemberService;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

    @Value("${taobaolive.httpsimulate:false}")
    private boolean httpsimulate;

    @Autowired
    private MemberService memberService;

    @Autowired
    private TaobaoApiService taobaoApiService;

    @Autowired
    private TaobaoAccountService taobaoAccountService;

    @Autowired
    private RankingService rankingService;

    @Autowired
    private Executor rankingExecutor;

    private CountDownLatch countDownLatch = null;

    private Thread monitorThread = null;

    private ScheduleJobEntity scheduleJobEntity = null;

    @Override
    public void run(ScheduleJobEntity scheduleJobEntity) {
        String params = scheduleJobEntity.getParams();

        RankingEntity rankingEntity = null;
        LiveRoomEntity liveRoomEntity = null;
        TaobaoAccountEntity activeAccountEntity = null;

        this.scheduleJobEntity = scheduleJobEntity;

        try {
            int taskId = Integer.parseInt(params);

            logger.info("打助力开始, TaskID=" + taskId);

            rankingEntity = rankingService.getById(taskId);
            rankingEntity.setStartTime(new Date());
            rankingEntity.setState(RankingEntityState.Running.getState());

            SysMember sysMember = memberService.getById(rankingEntity.getMemberId());

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            logger.info("任务详细，TaskID：" + taskId +
                    ", 淘口令：" + rankingEntity.getTaocode() +
                    "，直播间ID：" + rankingEntity.getLiveId() +
                    "，直播间名称：" + rankingEntity.getRoomName() +
                    ", 目标助力值：" + rankingEntity.getTargetScore() +
                    ", 起始助力值：" + rankingEntity.getStartScore() +
                    ", 是否加购：" + rankingEntity.isDoubleBuy() +
                    "，开始时间：" + sdf.format(rankingEntity.getStartTime()));

            List<TaobaoAccountEntity> taobaoAccountEntities = rankingService.availableAccounts(sysMember, rankingEntity.getLiveId());

            logger.info("TaskID：" + taskId + "，有效小号数：" + ((taobaoAccountEntities == null || taobaoAccountEntities.size() < 1) ? 0 : taobaoAccountEntities.size()));

            if (taobaoAccountEntities == null || taobaoAccountEntities.size() < 1) {
                rankingEntity.setState(RankingEntityState.Stopped.getState());
                return;
            }

            logger.info("获取小号信息, TaskID=" + taskId);

            liveRoomEntity = new LiveRoomEntity();

            liveRoomEntity.setLiveId(rankingEntity.getLiveId());
            liveRoomEntity.setAccountId(rankingEntity.getLiveAccountId());
            liveRoomEntity.getHierarchyData().setScopeId(rankingEntity.getLiveScopeId());
            liveRoomEntity.getHierarchyData().setSubScopeId(rankingEntity.getLiveSubScopeId());

//            LiveRoomEntity liveRoomEntity = null;
//
//            activeAccountEntity = taobaoAccountEntities.get(0);
//            R r = taobaoApiService.getLiveInfo(rankingEntity.getTaocode(), activeAccountEntity);
//            if (r.getCode() == ErrorCodes.SUCCESS) {
//                liveRoomEntity = (LiveRoomEntity) r.get("live_room");
//                if (liveRoomEntity != null) {
//                    taobaoApiService.getLiveProducts(liveRoomEntity, activeAccountEntity);
//                }
//            }

            activeAccountEntity = taobaoAccountEntities.get(0);
            taobaoApiService.getH5Token(activeAccountEntity);
            taobaoApiService.getLiveEntry(liveRoomEntity, activeAccountEntity);
            taobaoApiService.getLiveProducts(liveRoomEntity, activeAccountEntity, 10);

            if (simulate) {
                if (liveRoomEntity == null) {
                    rankingEntity.setState(RankingEntityState.Stopped.getState());
                    return;
                }

            } else {
                if (liveRoomEntity == null || !liveRoomEntity.isHasRankingListEntry()) {
                    rankingEntity.setState(RankingEntityState.Stopped.getState());
                    return;
                }
            }

            rankingEntity.setUpdatedTime(new Date());
            rankingService.updateById(rankingEntity);

            monitorThread = new Thread(new MonitorWorker(rankingEntity, liveRoomEntity, activeAccountEntity));
            monitorThread.start();

            int unitScore = rankingEntity.isDoubleBuy() ?
                    (RankingScore.DoubleBuyFollow.getScore() + RankingScore.DoubleBuyBuy.getScore() + RankingScore.DoubleBuyWatch.getScore()) :
                    (RankingScore.Follow.getScore() + RankingScore.Buy.getScore() + RankingScore.Watch.getScore());

            int leftScore = rankingEntity.getTargetScore();
            int startIndex = 0, endScore = 0;

            // 直到达到目标
            while (leftScore > 0 && this.isRunning(rankingEntity)) {
                int totalCount = leftScore / unitScore + 1;

                if (startIndex >= taobaoAccountEntities.size()) {
                    break;
                }

                totalCount = Math.min(taobaoAccountEntities.size() - startIndex, totalCount);

                countDownLatch = new CountDownLatch(totalCount);

                int assistCount = 0;
                int idx = 0;
                for (idx = startIndex; idx < startIndex + totalCount && idx < taobaoAccountEntities.size() && this.isRunning(rankingEntity); idx++) {
                    try {
                        TaobaoAccountEntity taobaoAccountEntity = taobaoAccountEntities.get(idx);
                        rankingExecutor.execute(
                                new AssistRunnable(rankingEntity, liveRoomEntity, taobaoAccountEntity)
                        );
                        assistCount++;
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        idx--;
                    }
                }

                // 万一被强制停止了呢？
                for (int left = idx; left < startIndex + totalCount && left < taobaoAccountEntities.size(); left++) {
                    countDownLatch.countDown();
                }

                countDownLatch.await();

                startIndex += assistCount;

                endScore = -1;
                if (simulate) {
                    endScore = liveRoomEntity.getRankingListData().getRankingScore();

                } else {
                    for (int retry = 0; retry < Config.MAX_RETRY; retry++) {
                        R r = taobaoApiService.getLiveEntry(liveRoomEntity, activeAccountEntity);
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

            logger.info("正在保存当前的状态, TaskID=" + taskId);
            // mark assisted account
            List<TaobaoAccountEntity> markedAccounts = new ArrayList<>();
            for (int idx1 = 0; idx1 < startIndex; idx1++) {
                markedAccounts.add(taobaoAccountEntities.get(idx1));
            }
            if (markedAccounts.size() > 0) {
                rankingService.markAssist(sysMember, liveRoomEntity.getLiveId(), markedAccounts);
            }

            rankingEntity.setEndScore(endScore);
            rankingEntity.setEndTime(new Date());
            rankingEntity.setUpdatedTime(new Date());
            if (leftScore > 0) {
                rankingEntity.setState(RankingEntityState.Stopped.getState());
            } else {
                rankingEntity.setState(RankingEntityState.Finished.getState());
            }
            rankingService.updateById(rankingEntity);

            monitorThread.join();

            logger.info("MonitorThread 结束, TaskID=" + taskId);

        } catch (Exception ex) {
            ex.printStackTrace();

            logger.error(ex.toString());

            rankingEntity.setState(RankingEntityState.Error.getState());

        } finally {
            if (rankingEntity != null) {
                if (liveRoomEntity != null && activeAccountEntity != null) {
                    updateScore(rankingEntity, liveRoomEntity, activeAccountEntity);
                }

                rankingEntity.setEndTime(new Date());
                rankingEntity.setUpdatedTime(new Date());
                rankingService.updateById(rankingEntity);
            }

            rankingService.deleteTask(rankingEntity);

            logger.info("打助力结束");
        }
    }

    private boolean isRunning(RankingEntity rankingEntity) {
        // return rankingService.isRunning(rankingEntity, scheduleJobEntity.getId());
        return this.scheduleJobEntity.getState() == ScheduleState.NORMAL.getValue() &&
                (rankingEntity.getEndScore() - rankingEntity.getStartScore() < rankingEntity.getTargetScore());
    }

    private void updateScore(RankingEntity rankingEntity, LiveRoomEntity liveRoomEntity, TaobaoAccountEntity activeAccountEntity) {
        if (simulate) {
            int currentScore = liveRoomEntity.getRankingListData().getRankingScore();
            rankingEntity.setEndScore(currentScore);
            rankingService.updateById(rankingEntity);

        } else {
            for (int retry = 0; retry < Config.MAX_RETRY; retry++) {
                R r = taobaoApiService.getLiveEntry(liveRoomEntity, activeAccountEntity);
                if (r.getCode() == ErrorCodes.SUCCESS) {
                    int currentScore = liveRoomEntity.getRankingListData().getRankingScore();

                    rankingEntity.setEndScore(currentScore);
                    rankingService.updateById(rankingEntity);
                    break;
                }
            }
        }
    }

    private synchronized void addScore(LiveRoomEntity liveRoomEntity, int score) {
        if (!simulate) {
            return;
        }
        liveRoomEntity.getRankingListData().setRankingScore(liveRoomEntity.getRankingListData().getRankingScore() + score);
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
                logger.info("AssistRunnable 成功启动：" + Thread.currentThread().getId());

                if (!isRunning(rankingEntity)) {
                    return;
                }

                // 清醒session
                {
                    if (httpsimulate) {
                        // 初始化
                        taobaoApiService.getH5Token(activeAccount);
                    } else {
                        Thread.sleep(100);
                    }
                }

                // 关注
                {
                    if (httpsimulate) {
                        R r = taobaoApiService.taskFollow(liveRoomEntity, activeAccount);
                        if (r.getCode() != ErrorCodes.SUCCESS) {
                            r = taobaoApiService.taskFollow(liveRoomEntity, activeAccount);
                        }
                        if (r.getCode() != ErrorCodes.SUCCESS) {
                            r = taobaoApiService.taskFollow(liveRoomEntity, activeAccount);
                        }
                    } else {
                        Thread.sleep(100);
                    }

                    int followScore = rankingEntity.isDoubleBuy() ? RankingScore.DoubleBuyFollow.getScore() : RankingScore.Follow.getScore();
                    addScore(liveRoomEntity, followScore);

                    logger.info(Thread.currentThread().getId() + " 加关注：" + followScore);
                }

                if (!isRunning(rankingEntity)) {
                    return;
                }

                // 观看停留
                {
                    int stayScore = rankingEntity.isDoubleBuy() ? RankingScore.DoubleBuyWatch.getScore() : RankingScore.Watch.getScore();
                    if (httpsimulate) {
                        if (stayScore > 0) {
                            for (int time = 60; time <= 3000; time += 60) {
                                R r = taobaoApiService.taskStay(liveRoomEntity, activeAccount, time);

                                logger.info(Thread.currentThread().getId() + " 停留：" + time + "秒");

//                        if (r.getCode() != ErrorCodes.SUCCESS) {
//                            r = taobaoApiService.taskStay(liveRoomEntity, activeAccount, time);
//                        }
//                        if (r.getCode() != ErrorCodes.SUCCESS) {
//                            r = taobaoApiService.taskStay(liveRoomEntity, activeAccount, time);
//                        }

                                if (!isRunning(rankingEntity)) {
                                    return;
                                }
                            }

                            addScore(liveRoomEntity, stayScore);
                        }
                    } else {
                        if (stayScore > 0) {
                            for (int time = 60; time <= 3000; time += 60) {
                                Thread.sleep(100);

                                if (!isRunning(rankingEntity)) {
                                    return;
                                }
                            }
                            addScore(liveRoomEntity, stayScore);
                        }
                    }
                    logger.info(Thread.currentThread().getId() + " 加观看停留：" + stayScore);
                }

                if (!isRunning(rankingEntity)) {
                    return;
                }

                // 购买
                {
                    int buyScore = rankingEntity.isDoubleBuy() ? RankingScore.DoubleBuyBuy.getScore() : RankingScore.Buy.getScore();
                    if (httpsimulate) {
                        if (buyScore > 0) {
                            List<ProductEntity> productEntities = liveRoomEntity.getProducts();
                            for (int idx = 0; idx < productEntities.size(); idx++) {
                                R r = taobaoApiService.taskBuy(liveRoomEntity, activeAccount, productEntities.get(idx).getProductId());
//                        if (r.getCode() != ErrorCodes.SUCCESS) {
//                            r = taobaoApiService.taskBuy(liveRoomEntity, activeAccount, productEntities.get(idx).getProductId());
//                        }
//                        if (r.getCode() != ErrorCodes.SUCCESS) {
//                            r = taobaoApiService.taskBuy(liveRoomEntity, activeAccount, productEntities.get(idx).getProductId());
//                        }

                                logger.info(Thread.currentThread().getId() + " 购买：第" + (idx+1) + "个");

                                if (!isRunning(rankingEntity)) {
                                    return;
                                }
                            }

                            addScore(liveRoomEntity, buyScore);
                        }

                    } else {
                        if (buyScore > 0) {
                            List<ProductEntity> productEntities = liveRoomEntity.getProducts();
                            for (int idx = 0; idx < productEntities.size(); idx++) {
                                Thread.sleep(100);

                                if (!isRunning(rankingEntity)) {
                                    return;
                                }
                            }
                        }
                    }
                    logger.info(Thread.currentThread().getId() + " 加购买：" + buyScore);
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                countDownLatch.countDown();

                logger.info("AssistRunnable 结束：" + Thread.currentThread().getId());
            }
        }

        private void assistRanking(RankingEntity rankingEntity, LiveRoomEntity liveRoomEntity, TaobaoAccountEntity activeAccount) {
            if (simulate) {
                simulateAssistRanking(rankingEntity, liveRoomEntity, activeAccount);
                return;
            }

            try {
                if (!isRunning(rankingEntity)) {
                    return;
                }

                // 初始化
                taobaoApiService.getH5Token(activeAccount);

                // 关注
                {
                    R r = taobaoApiService.taskFollow(liveRoomEntity, activeAccount);
//                    if (r.getCode() != ErrorCodes.SUCCESS) {
//                        r = taobaoApiService.taskFollow(liveRoomEntity, activeAccount);
//                    }
//                    if (r.getCode() != ErrorCodes.SUCCESS) {
//                        r = taobaoApiService.taskFollow(liveRoomEntity, activeAccount);
//                    }
                }

                if (!isRunning(rankingEntity)) {
                    return;
                }

                {
                    // 观看停留
                    for (int time = 60; time <= 3000; time += 60) {
                        R r = taobaoApiService.taskStay(liveRoomEntity, activeAccount, time);

//                        if (r.getCode() != ErrorCodes.SUCCESS) {
//                            r = taobaoApiService.taskStay(liveRoomEntity, activeAccount, time);
//                        }
//                        if (r.getCode() != ErrorCodes.SUCCESS) {
//                            r = taobaoApiService.taskStay(liveRoomEntity, activeAccount, time);
//                        }

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
//                        if (r.getCode() != ErrorCodes.SUCCESS) {
//                            r = taobaoApiService.taskBuy(liveRoomEntity, activeAccount, productEntities.get(idx).getProductId());
//                        }
//                        if (r.getCode() != ErrorCodes.SUCCESS) {
//                            r = taobaoApiService.taskBuy(liveRoomEntity, activeAccount, productEntities.get(idx).getProductId());
//                        }

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
        private TaobaoAccountEntity taobaoAccountEntity;

        public MonitorWorker(RankingEntity rankingEntity, LiveRoomEntity liveRoomEntity, TaobaoAccountEntity taobaoAccountEntity) {
            this.rankingEntity = rankingEntity;
            this.liveRoomEntity = liveRoomEntity;
            this.taobaoAccountEntity = taobaoAccountEntity;
        }

        @Override
        public void run() {
            while (rankingEntity.getState() == RankingEntityState.Running.getState()) {
                try {
                    updateScore(rankingEntity, liveRoomEntity, taobaoAccountEntity);

                    Thread.sleep(1000);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
