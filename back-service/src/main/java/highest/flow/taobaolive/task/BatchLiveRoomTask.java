package highest.flow.taobaolive.task;

import highest.flow.taobaolive.common.config.Config;
import highest.flow.taobaolive.common.defines.ErrorCodes;
import highest.flow.taobaolive.common.utils.HFStringUtils;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.job.entity.ScheduleJobEntity;
import highest.flow.taobaolive.taobao.defines.*;
import highest.flow.taobaolive.taobao.entity.*;
import highest.flow.taobaolive.taobao.service.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("batchLiveRoomTask")
public class BatchLiveRoomTask implements ITask {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${taobaolive.simulate:false}")
    private boolean simulate;

    @Autowired
    private MemberTaoAccService memberTaoAccService;

    @Autowired
    private TaobaoApiService taobaoApiService;

    @Autowired
    private TaobaoAccountService taobaoAccountService;

    @Autowired
    private LiveRoomStrategyService liveRoomStrategyService;

    @Autowired
    private LiveRoomHistoryService liveRoomHistoryService;

    @Autowired
    private LiveRoomProductService liveRoomProductService;

    @Autowired
    private ProductSearchService productSearchService;

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

            MemberTaoAccEntity memberTaoAccEntity = this.memberTaoAccService.getMemberByTaobaoAccountNick(taobaoAccountNick);

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

            TaobaoAccountEntity tempAccount = new TaobaoAccountEntity();
            taobaoApiService.getH5Token(tempAccount);

            // 等待直播中的直播间结束
            while (playingLiveRoom != null) {
                // R r = taobaoApiService.getLivePreGet(playingLiveRoom.getLiveId());
                R r = taobaoApiService.getLiveDetail(playingLiveRoom.getLiveId(), tempAccount);
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
            List<LiveRoomStrategyEntity> liveRoomStrategyEntities = this.liveRoomStrategyService.getLiveRoomStrategies(taobaoAccountNick);

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

                Date appointmentTime = liveRoomStrategyEntity.getLiveAppointmentTime();
                Date operationTime = memberTaoAccEntity.getOperationStartTime();

                Date today = new Date();
                Calendar calendar = Calendar.getInstance();
                calendar.set(today.getYear(), today.getMonth(), today.getDay(), appointmentTime.getHours(), appointmentTime.getMinutes(), appointmentTime.getSeconds());

                if (operationTime.getHours() > appointmentTime.getHours() || (operationTime.getHours() == appointmentTime.getHours() && operationTime.getMinutes() > appointmentTime.getMinutes())) {
                    // 下一天
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                }
                appointmentTime = calendar.getTime();

                liveRoomEntity.setLiveAppointmentTime(appointmentTime);

                this.liveRoomHistoryService.save(liveRoomEntity);

                liveRoomEntities.add(liveRoomEntity);
            }

            // 采集商品
            searchProducts(liveRoomEntities);

            this.taobaoApiService.getUserSimple(taobaoAccountEntity);

            for (LiveRoomEntity liveRoomEntity : liveRoomEntities) {
                if (liveRoomEntity.getLiveState() == LiveRoomState.Stopped.getState() ||
                        liveRoomEntity.getLiveState() == LiveRoomState.Deleted.getState()) {
                    continue;
                }

                if (liveRoomEntity.getLiveState() == LiveRoomState.Preparing.getState()) {
                    for (int idx = 0; idx < Config.MAX_RETRY; idx++) {
                        R r = this.taobaoApiService.createLiveRoomWeb(liveRoomEntity, taobaoAccountEntity);
                        if (r.getCode() == ErrorCodes.SUCCESS) {
                            LiveRoomEntity newLiveRoomEntity = (LiveRoomEntity) r.get("live_room");
                            liveRoomEntity.setLiveId(newLiveRoomEntity.getLiveId());
                            liveRoomEntity.setLiveAppointmentTime(newLiveRoomEntity.getLiveAppointmentTime());
                            liveRoomEntity.setLiveCoverImg(newLiveRoomEntity.getLiveCoverImg());
                            liveRoomEntity.setLiveCoverImg169(newLiveRoomEntity.getLiveCoverImg169());
                            liveRoomEntity.setLiveTitle(newLiveRoomEntity.getLiveTitle());
                            liveRoomEntity.setLiveIntro(newLiveRoomEntity.getLiveIntro());
                            liveRoomEntity.setLiveChannelId(newLiveRoomEntity.getLiveChannelId());
                            liveRoomEntity.setLiveColumnId(newLiveRoomEntity.getLiveColumnId());
                            liveRoomEntity.setLiveLocation(newLiveRoomEntity.getLiveLocation());

                            liveRoomEntity.setLiveState(LiveRoomState.Published.getState());

                            this.liveRoomHistoryService.updateById(liveRoomEntity);
                            break;

                        } else {
                            logger.error("[" + taobaoAccountNick + "] 发布预告失败");
                            continue;
                        }
                    }
                }

                if (liveRoomEntity.getLiveKind() == LiveRoomKind.Flow.getKind() && liveRoomEntity.getLiveState() == LiveRoomState.Published.getState()) {
                    for (int idx = 0; idx < Config.MAX_RETRY; idx++) {
                        R r = taobaoApiService.startLive(liveRoomEntity, taobaoAccountEntity);
                        if (r.getCode() == ErrorCodes.SUCCESS) {

                            liveRoomEntity.setLiveState(LiveRoomState.Started.getState());

                            this.liveRoomHistoryService.updateById(liveRoomEntity);
                            Thread.sleep(1000);
                            break;

                        } else {
                            logger.error("[" + taobaoAccountNick + "] 开始预告失败");
                            Thread.sleep(1000);
                        }
                    }
                }

                List<ProductEntity> products = liveRoomProductService.getProducts(liveRoomEntity.getLiveId());

                for (ProductEntity productEntity : products) {
                    String productId = productEntity.getProductId();
                    productId = StringUtils.strip(productId, "\r\n ");
                    productEntity.setProductId(productId);

                    for (int idxRetry = 0; idxRetry < Config.MAX_RETRY; idxRetry++) {
                        if (HFStringUtils.isNullOrEmpty(productEntity.getPicurl()) || HFStringUtils.isNullOrEmpty(productEntity.getTitle())) {
                            R r = this.taobaoApiService.getProductItemInfo(taobaoAccountEntity, productId);
                            if (r.getCode() == ErrorCodes.SUCCESS) {
                                String imgUrl = (String) r.get("img_url");
                                String title = (String) r.get("title");
                                String price = (String) r.get("price");

                                productEntity.setPicurl(imgUrl);
                                productEntity.setPrice(price);
                                productEntity.setTitle(title);
                            }
                        }

                        R r = this.taobaoApiService.publishProductWeb(liveRoomEntity, taobaoAccountEntity, productEntity);

                        if (r.getCode() == ErrorCodes.SUCCESS) {
                            productEntity.setState(ProductState.Active.getState());
                            break;
                        }
                    }
                }

                liveRoomEntity.setProducts(products);
                this.liveRoomProductService.saveProducts(liveRoomEntity.getLiveId(), products);

                if (liveRoomEntity.getLiveKind() == LiveRoomKind.Flow.getKind() && liveRoomEntity.getLiveState() == LiveRoomState.Started.getState()) {
                    for (int idxRetry = 0; idxRetry < Config.MAX_RETRY; idxRetry++) {
                        R r = this.taobaoApiService.stopLive(liveRoomEntity, taobaoAccountEntity);
                        if (r.getCode() == ErrorCodes.SUCCESS) {
                            liveRoomEntity.setLiveState(LiveRoomState.Stopped.getState());

                            this.liveRoomHistoryService.updateById(liveRoomEntity);

                            Thread.sleep(1000);
                            break;

                        } else {
                            logger.error("[" + taobaoAccountNick + "] 结束预告失败");
                            Thread.sleep(1000);
                        }
                    }
                }

                if (liveRoomEntity.getLiveKind() == LiveRoomKind.Flow.getKind() && liveRoomEntity.getLiveState() == LiveRoomState.Stopped.getState()) {
                    for (int idxRetry = 0; idxRetry < Config.MAX_RETRY; idxRetry++) {
                        R r = this.taobaoApiService.deleteLive(liveRoomEntity, taobaoAccountEntity);
                        if (r.getCode() == ErrorCodes.SUCCESS) {
                            liveRoomEntity.setLiveState(LiveRoomState.Deleted.getState());

                            this.liveRoomHistoryService.updateById(liveRoomEntity);

                            Thread.sleep(1000);
                            break;

                        } else {
                            logger.error("[" + taobaoAccountNick + "] 删除预告失败");
                            Thread.sleep(1000);
                        }
                    }
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            logger.info("发布批量发布预告任务结束");
        }
    }

    private void searchProducts(List<LiveRoomEntity> liveRoomEntities) {
        try {
            Set<String> productIds = new HashSet<>();

            for (LiveRoomEntity liveRoomEntity : liveRoomEntities) {
                List<ProductEntity> productEntities = this.liveRoomProductService.getProducts(liveRoomEntity.getLiveId());
                liveRoomEntity.setProducts(productEntities);

                for (ProductEntity productEntity : productEntities) {
                    productIds.add(productEntity.getProductId());
                }
            }

            for (LiveRoomEntity liveRoomEntity : liveRoomEntities) {
                if (liveRoomEntity.getProducts().size() >= liveRoomEntity.getPscProductCount()) {
                    continue;
                }

                List<ProductEntity> products = liveRoomEntity.getProducts();

                int startAt = 0;
                ProductCategory productCategory = productSearchService.getCategory(liveRoomEntity.getPscChannelId(),
                        liveRoomEntity.getPscCategoryId());

                while (products.size() < liveRoomEntity.getPscProductCount()) {
                    List<ProductEntity> searchProducts = productSearchService.searchProducts(productCategory,
                            ProductSearchSortKind.from(liveRoomEntity.getPscSortKind()),
                            startAt,
                            liveRoomEntity.getPscStartPrice(),
                            liveRoomEntity.getPscEndPrice(),
                            liveRoomEntity.getPscMinSales(),
                            liveRoomEntity.isPscIsTmall());
                    if (searchProducts == null || searchProducts.size() < 1) {
                        break;
                    }

                    for (ProductEntity productEntity : searchProducts) {
                        if (productIds.contains(productEntity.getProductId())) {
                            continue;
                        }
                        if (products.size() >= liveRoomEntity.getPscProductCount()) {
                            break;
                        }

                        products.add(productEntity);
                        productIds.add(productEntity.getProductId());
                    }

                    startAt += searchProducts.size();
                }

                liveRoomEntity.setProducts(products);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
