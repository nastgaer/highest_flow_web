package highest.flow.taobaolive.task;

import highest.flow.taobaolive.common.config.Config;
import highest.flow.taobaolive.common.defines.ErrorCodes;
import highest.flow.taobaolive.common.exception.RRException;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.job.defines.ScheduleState;
import highest.flow.taobaolive.job.entity.ScheduleJobEntity;
import highest.flow.taobaolive.sys.entity.SysMember;
import highest.flow.taobaolive.sys.service.MemberService;
import highest.flow.taobaolive.taobao.defines.RankingEntityState;
import highest.flow.taobaolive.taobao.defines.RankingScore;
import highest.flow.taobaolive.taobao.entity.LiveRoomEntity;
import highest.flow.taobaolive.taobao.entity.ProductEntity;
import highest.flow.taobaolive.taobao.entity.RankingEntity;
import highest.flow.taobaolive.taobao.entity.TaobaoAccountEntity;
import highest.flow.taobaolive.taobao.service.RankingService;
import highest.flow.taobaolive.taobao.service.TaobaoAccountService;
import highest.flow.taobaolive.taobao.service.TaobaoApiService;

import org.apache.http.cookie.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

@Component("assistRankingTask")
@Scope("prototype")  // 不会Singleton
public class AssistRankingTask implements ITask {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${ranking.unit-pool-size:300}")
    private int maxUnitPoolSize; // 一个刷任务的最多线程数

    @Autowired
    private MemberService memberService;

    @Autowired
    private TaobaoApiService taobaoLiveApiService;

    @Autowired
    private TaobaoAccountService taobaoAccountService;

    @Autowired
    private RankingService rankingService;

    @Autowired
    private Executor rankingExecutor;

    private CountDownLatch countDownLatch = null;

    private Thread monitorThread = null;

    private ScheduleJobEntity scheduleJobEntity = null;

    @PostConstruct
    public void init() {

    }

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
            if (rankingEntity == null) {
                throw new RRException("找不到任务" + params);
            }

            liveRoomEntity = new LiveRoomEntity();

            liveRoomEntity.setLiveId(rankingEntity.getLiveId());
            liveRoomEntity.setAccountId(rankingEntity.getAccountId());
            liveRoomEntity.setAccountName(rankingEntity.getRoomName());

            // 获取开始当前的赛道状态
            List<TaobaoAccountEntity> activeAccounts = this.taobaoAccountService.getActivesByMember(null, Config.MAX_RETRY_ACCOUNTS);
            for (int retry = 0; activeAccounts != null && retry < activeAccounts.size(); retry++) {
                activeAccountEntity = activeAccounts.get(retry);
                this.taobaoLiveApiService.getH5Token(activeAccountEntity);
                R r = this.taobaoLiveApiService.getRankingListData(liveRoomEntity, activeAccountEntity);
                if (r.getCode() == ErrorCodes.SUCCESS)
                    break;
            }

            if (!liveRoomEntity.isHasHourRankingListEntry()) {
                throw new RRException("该直播间没有赛道");
            }

            for (int retry = 0; retry < Config.MAX_RETRY; retry++) {
                R r = taobaoLiveApiService.getLiveProducts(liveRoomEntity, activeAccountEntity, 10);
                if (r.getCode() != ErrorCodes.SUCCESS) {
                    logger.error(activeAccountEntity.getNick() + ": getLiveProductsWeb" + r.getMsg());
                    r = taobaoLiveApiService.getLiveProductsWeb(liveRoomEntity, activeAccountEntity, 10);
                }
                if (r.getCode() == ErrorCodes.SUCCESS) {
                    break;
                }
                logger.error(activeAccountEntity.getNick() + ": getLiveProductsWeb" + r.getMsg());
            }
            if (rankingEntity.isHasBuy() && liveRoomEntity.getProducts().size() < 1) {
                throw new RRException("获取直播间商品信息失败");
            }

            rankingEntity.setStartScore(liveRoomEntity.getHourRankingListData().getRankingScore());
            rankingEntity.setEndScore(liveRoomEntity.getHourRankingListData().getRankingScore());

            rankingEntity.setStartTime(new Date());
            rankingEntity.setState(RankingEntityState.Running.getState());

            SysMember sysMember = memberService.getById(rankingEntity.getMemberId());

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            logger.info("任务详细，TaskID：" + taskId +
                    "，直播间ID：" + rankingEntity.getLiveId() +
                    "，直播间名称：" + rankingEntity.getRoomName() +
                    ", 起始助力值：" + rankingEntity.getStartScore() +
                    ", 目标助力值：" + rankingEntity.getTargetScore() +
                    ", 助力专项：" + (rankingEntity.isHasFollow() ? "关注, " : "") + (rankingEntity.isHasStay() ? "停留, " : "") +
                            (rankingEntity.isHasBuy() ? "购买, " : "") + (rankingEntity.isHasDoubleBuy() ? "加购" : "") +
                    "，开始时间：" + sdf.format(rankingEntity.getStartTime()));

            List<TaobaoAccountEntity> taobaoAccountEntities = rankingService.availableAccounts(sysMember, rankingEntity.getLiveId());

            logger.info("TaskID：" + taskId + "，有效小号数：" + ((taobaoAccountEntities == null || taobaoAccountEntities.size() < 1) ? 0 : taobaoAccountEntities.size()));

            if (taobaoAccountEntities == null || taobaoAccountEntities.size() < 1) {
                rankingEntity.setState(RankingEntityState.Stopped.getState());
                return;
            }

            logger.info("获取小号信息, TaskID=" + taskId);

            rankingEntity.setUpdatedTime(new Date());
            rankingService.updateById(rankingEntity);

            monitorThread = new Thread(new MonitorWorker(rankingEntity, liveRoomEntity, activeAccountEntity));
            monitorThread.start();

            int unitScore = rankingEntity.isHasDoubleBuy() ? this.rankingService.getRankingUnitScore(RankingScore.DoubleBuy) :
                    (rankingEntity.isHasBuy() ? this.rankingService.getRankingUnitScore(RankingScore.Buy) : 0);
            unitScore += rankingEntity.isHasFollow() ? this.rankingService.getRankingUnitScore(RankingScore.Follow) : 0;
            unitScore += rankingEntity.isHasStay() ? this.rankingService.getRankingUnitScore(RankingScore.Stay) : 0;

            int leftScore = rankingEntity.getTargetScore();
            int startIndex = 0, endScore = 0;

            ConcurrentLinkedQueue<TaobaoAccountEntity> concurrentLinkedQueue = new ConcurrentLinkedQueue<>();

            // 直到达到目标
            while (leftScore > 0 && this.isRunning(rankingEntity)) {
                int totalCount = leftScore / unitScore + 1;

                if (startIndex >= taobaoAccountEntities.size()) {
                    break;
                }

                totalCount = Math.min(maxUnitPoolSize, Math.min(taobaoAccountEntities.size() - startIndex, totalCount));

                countDownLatch = new CountDownLatch(totalCount);

                int assistCount = 0;
                int idx = 0;
                for (idx = startIndex; idx < startIndex + totalCount && idx < taobaoAccountEntities.size() && this.isRunning(rankingEntity); idx++) {
                    try {
                        TaobaoAccountEntity taobaoAccountEntity = taobaoAccountEntities.get(idx);
                        rankingExecutor.execute(
                                new AssistRunnable(rankingEntity, liveRoomEntity, taobaoAccountEntity, concurrentLinkedQueue)
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

                if (!this.isRunning(rankingEntity))
                    break;

                try {
                    Thread.sleep(2 * 1000);
                } catch (Exception ex) {}

                for (int retry = 0; retry < Config.MAX_RETRY; retry++) {
                    R r = taobaoLiveApiService.getRankingListData(liveRoomEntity, activeAccountEntity);
                    if (r.getCode() == ErrorCodes.SUCCESS) {
                        endScore = liveRoomEntity.getHourRankingListData().getRankingScore();
                        break;
                    }
                    logger.error(activeAccountEntity.getNick() + ": getRankingListData" + r.getMsg());
                }

                leftScore = rankingEntity.getTargetScore() - (endScore - rankingEntity.getStartScore());
                if (endScore < 0) {
                    break;
                }
            }

            logger.info("正在保存当前的状态, TaskID=" + taskId);
            // 标记已刷的小号为已读
            List<TaobaoAccountEntity> markedAccounts = new ArrayList<>();
            for (TaobaoAccountEntity taobaoAccountEntity : concurrentLinkedQueue) {
                markedAccounts.add(taobaoAccountEntity);
            }
            if (markedAccounts.size() > 0) {
                rankingService.markAssist(sysMember, liveRoomEntity.getLiveId(), markedAccounts);
            }

            endScore = liveRoomEntity.getHourRankingListData().getRankingScore();
            leftScore = rankingEntity.getTargetScore() - (endScore - rankingEntity.getStartScore());

            rankingEntity.setEndScore(endScore);
            rankingEntity.setEndTime(new Date());
            rankingEntity.setUpdatedTime(new Date());
            if (leftScore > 0) { // 还没刷完
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

            if (ex instanceof RRException)
                rankingEntity.setMsg(((RRException)ex).getMsg());

        } finally {
            if (rankingEntity != null) {
                rankingEntity.setEndTime(new Date());
                rankingEntity.setUpdatedTime(new Date());
                rankingService.updateById(rankingEntity);
            }
            // 设置定时任务状态为停止
            rankingService.stopTask(rankingEntity);

            logger.info("打助力结束");
        }
    }

    private boolean isRunning(RankingEntity rankingEntity) {
        // return rankingService.isRunning(rankingEntity, scheduleJobEntity.getId());
        return this.scheduleJobEntity.getState() == ScheduleState.NORMAL.getValue() &&
                (rankingEntity.getEndScore() - rankingEntity.getStartScore() < rankingEntity.getTargetScore());
    }

    private void updateScore(RankingEntity rankingEntity, LiveRoomEntity liveRoomEntity, TaobaoAccountEntity activeAccountEntity) {
        for (int retry = 0; retry < Config.MAX_RETRY; retry++) {
            R r = taobaoLiveApiService.getRankingListData(liveRoomEntity, activeAccountEntity);
            if (r.getCode() == ErrorCodes.SUCCESS) {
                int currentScore = liveRoomEntity.getHourRankingListData().getRankingScore();

                rankingEntity.setEndScore(currentScore);
                rankingEntity.setUpdatedTime(new Date());
                rankingService.updateById(rankingEntity);
                break;
            }
            logger.error(activeAccountEntity.getNick() + ": getRankingListData" + r.getMsg());
        }
    }

    class AssistRunnable implements Runnable {

        private RankingEntity rankingEntity;
        private LiveRoomEntity liveRoomEntity;
        private TaobaoAccountEntity taobaoAccountEntity;
        private ConcurrentLinkedQueue<TaobaoAccountEntity> rankedAccounts;

        public AssistRunnable(RankingEntity rankingEntity, LiveRoomEntity liveRoomEntity, TaobaoAccountEntity activeAccount, ConcurrentLinkedQueue<TaobaoAccountEntity> rankedAccounts) {
            this.rankingEntity = rankingEntity;
            this.liveRoomEntity = liveRoomEntity;
            this.taobaoAccountEntity = activeAccount;
            this.rankedAccounts = rankedAccounts;
        }

        @Override
        public void run() {
            assistRanking(rankingEntity,
                    liveRoomEntity,
                    taobaoAccountEntity);
        }

        private void assistRanking(RankingEntity rankingEntity, LiveRoomEntity liveRoomEntity, TaobaoAccountEntity activeAccount) {
            try {
                if (!isRunning(rankingEntity)) {
                    return;
                }

                // 初始化
                taobaoLiveApiService.getH5Token(activeAccount);

                for (int retry = 0; retry < Config.MAX_RETRY; retry++) {
                    R r = taobaoLiveApiService.getIntimacyDetail(liveRoomEntity, activeAccount);
                    if (r.getCode() == ErrorCodes.SUCCESS) {
                        break;
                    }
                    logger.error(activeAccount.getNick() + ": getIntimacyDetail" + r.getMsg());
                }

                if (rankingEntity.isHasFollow())
                {
                    // 关注
                    R r = taobaoLiveApiService.taskFollow(liveRoomEntity, activeAccount);
                    if (r.getCode() != ErrorCodes.SUCCESS) {
                        logger.error(activeAccount.getNick() + ": taskFollow" + r.getMsg());
                        r = taobaoLiveApiService.taskFollow(liveRoomEntity, activeAccount);
                    }
                    if (r.getCode() != ErrorCodes.SUCCESS) {
                        logger.error(activeAccount.getNick() + ": taskFollow" + r.getMsg());
                        r = taobaoLiveApiService.taskFollow(liveRoomEntity, activeAccount);
                    }
                    if (r.getCode() != ErrorCodes.SUCCESS) {
                        logger.error(activeAccount.getNick() + ": taskFollow" + r.getMsg());
                    }
                    for (int retry = 0; retry < Config.MAX_RETRY; retry++) {
                        r = taobaoLiveApiService.taskComplete(activeAccount);
                        if (r.getCode() == ErrorCodes.SUCCESS) {
                            break;
                        }
                        logger.error(activeAccount.getNick() + ": taskComplete" + r.getMsg());
                    }
                }

                if (!isRunning(rankingEntity)) {
                    return;
                }

                if (rankingEntity.isHasBuy())
                {
                    // 购买
                    List<ProductEntity> productEntities = liveRoomEntity.getProducts();
                    if (productEntities != null && productEntities.size() > 0) {
                        String productId = productEntities.get(0).getProductId();
                        for (int idx = 0; idx < 10; idx++) {
                            R r = taobaoLiveApiService.taskBuy(liveRoomEntity, activeAccount, productId);
                            if (r.getCode() != ErrorCodes.SUCCESS) {
                                logger.error(activeAccount.getNick() + ": taskBuy" + r.getMsg());
                                r = taobaoLiveApiService.taskBuy(liveRoomEntity, activeAccount, productId);
                            }
                            if (r.getCode() != ErrorCodes.SUCCESS) {
                                logger.error(activeAccount.getNick() + ": taskBuy" + r.getMsg());
                                r = taobaoLiveApiService.taskBuy(liveRoomEntity, activeAccount, productId);
                            }
                            if (r.getCode() != ErrorCodes.SUCCESS) {
                                logger.error(activeAccount.getNick() + ": taskBuy" + r.getMsg());
                            }

                            if (!isRunning(rankingEntity)) {
                                return;
                            }
                        }
                    }
                }

                if (!isRunning(rankingEntity)) {
                    return;
                }

                if (rankingEntity.isHasStay())
                {
                    // 观看停留
                    for (int time = 60; time <= 3600; time += 60) {
                        R r = taobaoLiveApiService.taskStay(liveRoomEntity, activeAccount, time);
                        if (r.getCode() != ErrorCodes.SUCCESS) {
                            r = taobaoLiveApiService.taskStay(liveRoomEntity, activeAccount, time);
                        }
                        if (r.getCode() != ErrorCodes.SUCCESS) {
                            r = taobaoLiveApiService.taskStay(liveRoomEntity, activeAccount, time);
                        }
                        if (r.getCode() != ErrorCodes.SUCCESS) {
                            logger.error(activeAccount.getNick() + ": taskStay" + r.getMsg());
                        }

                        if (!isRunning(rankingEntity)) {
                            return;
                        }
                    }
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                this.rankedAccounts.add(activeAccount);

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

                    Thread.sleep(500);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
