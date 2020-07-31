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

    private MemberTaoAccEntity memberTaoAccEntity;

    private String taobaoAccountNick = "";

    @Override
    public void run(ScheduleJobEntity scheduleJobEntity) {
        String params = scheduleJobEntity.getParams();
        logger.info("开始发布批量发布预告任务, 参数=" + params);

        taobaoAccountNick = params;

        try {
            // 等待直到该账号直播结束
            TaobaoAccountEntity taobaoAccountEntity = taobaoAccountService.getInfo(taobaoAccountNick);
            if (taobaoAccountEntity == null) {
                logger.error("引流操作，找不到账号，昵称：" + taobaoAccountNick);
                return;
            }
            if (taobaoAccountEntity.getState() != TaobaoAccountState.Normal.getState()) {
                logger.error("引流操作，账号过期，昵称：" + taobaoAccountNick);
                return;
            }

            memberTaoAccEntity = this.memberTaoAccService.getMemberByTaobaoAccountNick(taobaoAccountNick);

            // 查看服务周期
            if (memberTaoAccEntity.getState() != ServiceState.Normal.getState()) {
                logger.error("引流操作，服务过期，昵称：" + taobaoAccountNick);
                return;
            }

            if (memberTaoAccEntity.getServiceEndDate().getTime() < new Date().getTime()) {
                logger.error("引流操作，服务过期，昵称：" + taobaoAccountNick);
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

            taobaoApiService.getH5Token(taobaoAccountEntity);

            // 等待直播中的直播间结束
            while (playingLiveRoom != null) {
                // R r = taobaoApiService.getLivePreGet(playingLiveRoom.getLiveId());
                R r = taobaoApiService.getLiveDetailWeb(playingLiveRoom.getLiveId(), taobaoAccountEntity);
                if (r.getCode() == ErrorCodes.SUCCESS) {
                    LiveRoomEntity roomEntity = (LiveRoomEntity) r.get("live_room");
                    if (roomEntity.getLiveState() == LiveRoomState.Stopped.getState() ||
                            roomEntity.getLiveState() == LiveRoomState.Deleted.getState()) {
                        break;
                    }
                }
                Thread.sleep(60 * 1000);
            }

            List<LiveRoomEntity> liveRoomEntities = this.liveRoomHistoryService.getTodays(new Date());

            // 直播间已经结束了
            List<LiveRoomStrategyEntity> liveRoomStrategyEntities = this.liveRoomStrategyService.getLiveRoomStrategies(memberTaoAccEntity);

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
                today.setHours(appointmentTime.getHours());
                today.setMinutes(appointmentTime.getMinutes());
                today.setSeconds(appointmentTime.getSeconds());

                if (operationTime.getHours() > appointmentTime.getHours() || (operationTime.getHours() == appointmentTime.getHours() && operationTime.getMinutes() > appointmentTime.getMinutes())) {
                    // 下一天
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(today);
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                    today = calendar.getTime();
                }
                appointmentTime = today;

                liveRoomEntity.setLiveAppointmentTime(appointmentTime);

                this.liveRoomHistoryService.save(liveRoomEntity);

                liveRoomEntities.add(liveRoomEntity);
            }

            // 采集商品
            searchProducts(liveRoomEntities);

            this.taobaoApiService.getUserSimple(taobaoAccountEntity);

            for (LiveRoomEntity liveRoomEntity : liveRoomEntities) {
                if (!isRunnable()) {
                    break;
                }

                if (liveRoomEntity.getLiveState() == LiveRoomState.Stopped.getState() ||
                        liveRoomEntity.getLiveState() == LiveRoomState.Deleted.getState()) {
                    continue;
                }

                if (liveRoomEntity.getLiveState() == LiveRoomState.Preparing.getState()) {
                    String liveId = "";
                    for (int idx = 0; idx < Config.MAX_RETRY; idx++) {
                        R r = this.taobaoApiService.createLiveRoomWeb(liveRoomEntity, taobaoAccountEntity);
                        if (r.getCode() == ErrorCodes.SUCCESS) {
                            LiveRoomEntity newLiveRoomEntity = (LiveRoomEntity) r.get("live_room");
                            liveId = newLiveRoomEntity.getLiveId();
                            liveRoomEntity.setLiveId(liveId);
                            liveRoomEntity.setLiveState(LiveRoomState.Published.getState());
                            break;

                        } else {
                            logger.error("[" + taobaoAccountNick + "] 发布预告失败");
                            continue;
                        }
                    }

                    if (!HFStringUtils.isNullOrEmpty(liveId)) {
                        // 获取直播间详细内容
                        for (int idx = 0; idx < Config.MAX_RETRY; idx++) {
                            R r = this.taobaoApiService.getLiveDetailWeb(liveId, taobaoAccountEntity);
                            if (r.getCode() == ErrorCodes.SUCCESS) {
                                LiveRoomEntity createdLive = (LiveRoomEntity) r.get("live_room");
                                String accountId = createdLive.getAccountId();
                                String accountName = createdLive.getAccountName();
                                String topic = createdLive.getTopic();
                                liveRoomEntity.setAccountId(accountId);
                                liveRoomEntity.setAccountName(accountName);
                                liveRoomEntity.setTopic(topic);
                                break;

                            } else {
                                logger.error("[" + taobaoAccountNick + "] 获取预告详细失败");
                                continue;
                            }
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

                List<ProductEntity> products = liveRoomProductService.getProducts(liveRoomEntity);

                for (ProductEntity productEntity : products) {
                    String productId = productEntity.getProductId();
                    productId = StringUtils.strip(productId, "\r\n ");
                    productEntity.setProductId(productId);
                    productEntity.setLiveId(liveRoomEntity.getLiveId());

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
                this.liveRoomProductService.saveProducts(products);

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

            if (!isRunnable()) {
                // 删除预定任务
                this.liveRoomStrategyService.stopTask(memberTaoAccEntity);
            }
        }
    }

    private boolean isRunnable() {
        if (memberTaoAccEntity.getState() != ServiceState.Normal.getState()) {
            logger.info("引流操作，服务过期，昵称：" + taobaoAccountNick);
            return false;
        }

        if (memberTaoAccEntity.getServiceEndDate().getTime() < new Date().getTime()) {
            logger.info("引流操作，服务过期，昵称：" + taobaoAccountNick);
            return false;
        }
        return true;
    }

    private void searchProducts(List<LiveRoomEntity> liveRoomEntities) {
        try {
            Set<String> productIds = new HashSet<>();
            Map<ProductCategory, Integer> startAtMap = new HashMap<>();

            for (LiveRoomEntity liveRoomEntity : liveRoomEntities) {
                List<ProductEntity> productEntities = this.liveRoomProductService.getProducts(liveRoomEntity);
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


                ProductCategory productCategory = productSearchService.getCategory(liveRoomEntity.getPscChannelId(),
                        liveRoomEntity.getPscCategoryId());
                int startAt = 0;
                for (ProductCategory pc : startAtMap.keySet()) {
                    if (pc.compareTo(productCategory)) {
                        startAt = startAtMap.get(pc);
                        productCategory = pc;
                        break;
                    }
                }

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

                for (ProductEntity productEntity : products) {
                    productEntity.setHistoryId(liveRoomEntity.getId());
                }
                liveRoomEntity.setProducts(products);

                // 保存当前的搜索状态
                startAtMap.put(productCategory, startAt);

                this.liveRoomProductService.saveProducts(products);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
