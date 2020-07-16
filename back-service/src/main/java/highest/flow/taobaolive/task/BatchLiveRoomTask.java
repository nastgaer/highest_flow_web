package highest.flow.taobaolive.task;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import highest.flow.taobaolive.common.config.Config;
import highest.flow.taobaolive.common.defines.ErrorCodes;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.job.entity.ScheduleJobEntity;
import highest.flow.taobaolive.taobao.defines.LiveRoomState;
import highest.flow.taobaolive.taobao.defines.RankingEntityState;
import highest.flow.taobaolive.taobao.defines.RankingScore;
import highest.flow.taobaolive.taobao.defines.TaobaoAccountState;
import highest.flow.taobaolive.taobao.entity.*;
import highest.flow.taobaolive.taobao.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

@Component("batchLiveRoomTask")
public class BatchLiveRoomTask implements ITask {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${taobaolive.simulate:false}")
    private boolean simulate;

    @Autowired
    private TaobaoApiService taobaoApiService;

    @Autowired
    private TaobaoAccountService taobaoAccountService;

    @Autowired
    private LiveRoomStrategyService liveRoomStrategyService;

    @Autowired
    private LiveRoomHistoryService liveRoomHistoryService;

    @Override
    public void run(ScheduleJobEntity scheduleJobEntity) {
        String params = scheduleJobEntity.getParams();
        logger.info("开始发布批量发布预告任务, 参数=" + params);

        try {
            String taobaoAccountNick = params;

            // 等待直到该账号直播结束
            TaobaoAccountEntity taobaoAccountEntity = taobaoAccountService.getInfo(taobaoAccountNick);
            if (taobaoAccountEntity == null) {
                logger.error("找不到账号");
                return;
            }
            if (taobaoAccountEntity.getState() != TaobaoAccountState.Normal.getState()) {
                logger.error("账号过期");
                return;
            }

            LiveRoomEntity playingLiveRoom = null;
            int retry = 0;
            for (; retry < Config.MAX_RETRY; retry++) {
                R r = taobaoApiService.getPlayingLiveRoom(taobaoAccountEntity);
                if (r.getCode() != ErrorCodes.SUCCESS) {
                    continue;
                }

                playingLiveRoom = (LiveRoomEntity) r.get("live_room");
                break;
            }

            if (retry >= Config.MAX_RETRY) {
                logger.error("获取直播中的直播间");
                return;
            }

            // 等待直播中的直播间结束
            while (playingLiveRoom != null) {
                R r = taobaoApiService.getLiveDetail(playingLiveRoom.getLiveId());
                if (r.getCode() == ErrorCodes.SUCCESS) {
                    int roomStatus = (int) r.get("room_status");
                    if (roomStatus == LiveRoomState.Stopped.getState() ||
                            roomStatus == LiveRoomState.Deleted.getState()) {
                        break;
                    }
                }
                Thread.sleep(60 * 1000);
            }

            List<LiveRoomEntity> liveRoomEntities = this.liveRoomHistoryService.getTodays(new Date());

            // 直播间已经结束了
            List<LiveRoomStrategyEntity> liveRoomStrategyEntities = this.liveRoomStrategyService.getStrategy(taobaoAccountNick);

            for (int idx = liveRoomEntities.size(); idx < liveRoomStrategyEntities.size(); idx++) {
                LiveRoomStrategyEntity liveRoomStrategyEntity = liveRoomStrategyEntities.get(idx);

                LiveRoomEntity liveRoomEntity = new LiveRoomEntity();
                liveRoomEntity.setTaobaoAccountNick(liveRoomStrategyEntity.getTaobaoAccountNick());
                liveRoomEntity.setLiveKind(liveRoomStrategyEntity.getLiveKind());
                liveRoomEntity.setLiveId("");
                liveRoomEntity.setLiveCoverImg(liveRoomStrategyEntity.getLiveCoverImg());
                liveRoomEntity.setLiveCoverImg169(liveRoomStrategyEntity.getLiveCoverImg169());
                liveRoomEntity.setLiveTitle(liveRoomStrategyEntity.getLiveTitle());
                liveRoomEntity.setLiveIntro(liveRoomStrategyEntity.getLiveIntro());
                liveRoomEntity.setLiveAppointmentTime(liveRoomStrategyEntity.getLiveAppointmentTime());
                liveRoomEntity.setLiveChannelId(liveRoomStrategyEntity.getLiveChannelId());
                liveRoomEntity.setLiveColumnId(liveRoomStrategyEntity.getLiveColumnId());
                liveRoomEntity.setLiveLocation(liveRoomStrategyEntity.getLiveLocation());
                liveRoomEntity.setHotProductUrl(liveRoomStrategyEntity.getHotProductUrl());
                liveRoomEntity.setPscChannelId(liveRoomStrategyEntity.getPscChannelId());
                liveRoomEntity.setPscCategoryId(liveRoomStrategyEntity.getPscCategoryId());
                liveRoomEntity.setPscStartPrice(liveRoomStrategyEntity.getPscStartPrice());
                liveRoomEntity.setPscEndPrice(liveRoomStrategyEntity.getPscEndPrice());
                liveRoomEntity.setPscMinSales(liveRoomStrategyEntity.getPscMinSales());
                liveRoomEntity.setPscProductCount(liveRoomStrategyEntity.getPscProductCount());
                liveRoomEntity.setPscIsTmall(liveRoomStrategyEntity.isPscIsTmall());
                liveRoomEntity.setPscSortKind(liveRoomStrategyEntity.getPscSortKind());
                liveRoomEntity.setLiveState(LiveRoomState.Preparing.getState());
                liveRoomEntity.setCreatedTime(new Date());
                liveRoomEntity.setUpdatedTime(new Date());

                this.liveRoomHistoryService.save(liveRoomEntity);

                liveRoomEntities.add(liveRoomEntity);
            }

            this.taobaoApiService.getUserSimple(taobaoAccountEntity);

            for (LiveRoomEntity liveRoomEntity : liveRoomEntities) {
                if (liveRoomEntity.getLiveState() == LiveRoomState.Stopped.getState() ||
                        liveRoomEntity.getLiveState() == LiveRoomState.Deleted.getState()) {
                    continue;
                }

                if (liveRoomEntity.getLiveState() == LiveRoomState.Preparing.getState()) {
                    Date appointmentTime = liveRoomEntity.getLiveAppointmentTime();
                    // TODO
                    liveRoomEntity.setLiveAppointmentTime(appointmentTime);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            logger.info("发布批量发布预告任务结束");
        }
    }
}
