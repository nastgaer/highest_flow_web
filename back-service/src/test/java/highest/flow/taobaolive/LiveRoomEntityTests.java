package highest.flow.taobaolive;

import com.fasterxml.jackson.databind.ObjectMapper;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.taobao.defines.LiveRoomState;
import highest.flow.taobaolive.taobao.defines.TaobaoAccountState;
import highest.flow.taobaolive.taobao.entity.LiveRoomEntity;
import highest.flow.taobaolive.taobao.entity.PreLiveRoomSpecEntity;
import highest.flow.taobaolive.taobao.entity.ProductEntity;
import highest.flow.taobaolive.taobao.entity.TaobaoAccountEntity;
import highest.flow.taobaolive.taobao.service.TaobaoAccountService;
import highest.flow.taobaolive.taobao.service.TaobaoApiService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.List;

@SpringBootTest(classes = TaobaoliveApplication.class, args = "test")
public class LiveRoomEntityTests {

    @Autowired
    private TaobaoApiService taobaoApiService;

    @Autowired
    private TaobaoAccountService taobaoAccountService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    void getProductItem() {
        try {
            TaobaoAccountEntity activeAccount = null;
            List<TaobaoAccountEntity> taobaoAccountEntities = taobaoAccountService.list();
            for (TaobaoAccountEntity taobaoAccountEntity : taobaoAccountEntities) {
                if (taobaoAccountEntity.getState() == TaobaoAccountState.Normal.getState()) {
                    activeAccount = taobaoAccountEntity;
                    break;
                }
            }

            if (activeAccount != null) {
                taobaoApiService.getH5Token(activeAccount);

                R r = taobaoApiService.getProductItemInfo(activeAccount, "618917018752");

                ObjectMapper objectMapper = new ObjectMapper();
                System.out.println(objectMapper.writeValueAsString(r));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    void openProduct() {
        try {
            TaobaoAccountEntity activeAccount = null;
            List<TaobaoAccountEntity> taobaoAccountEntities = taobaoAccountService.list();
            for (TaobaoAccountEntity taobaoAccountEntity : taobaoAccountEntities) {
                if (taobaoAccountEntity.getState() == TaobaoAccountState.Normal.getState()) {
                    activeAccount = taobaoAccountEntity;
                    System.out.println("选择用户：" + activeAccount.getNick());
                    break;
                }
            }

            if (activeAccount != null) {
                taobaoApiService.getH5Token(activeAccount);

                ObjectMapper objectMapper = new ObjectMapper();

                R r = taobaoApiService.getProductItemInfo(activeAccount, "610704107634");
                System.out.println(objectMapper.writeValueAsString(r));

                r = taobaoApiService.openProduct(activeAccount, "610704107634");
                System.out.println(objectMapper.writeValueAsString(r));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    void createLiveRoom() {
        try {
            TaobaoAccountEntity activeAccount = null;
            List<TaobaoAccountEntity> taobaoAccountEntities = taobaoAccountService.list();
            for (TaobaoAccountEntity taobaoAccountEntity : taobaoAccountEntities) {
                if (taobaoAccountEntity.getState() == TaobaoAccountState.Normal.getState() &&
                        taobaoAccountEntity.getNick().equalsIgnoreCase("zhaoxiangchi00")) {
                    activeAccount = taobaoAccountEntity;
                    System.out.println("选择用户：" + activeAccount.getNick());
                    break;
                }
            }
            if (activeAccount == null) {
                System.out.println("好不到西晶香");
                return;
            }

            PreLiveRoomSpecEntity preLiveRoomSpecEntity = new PreLiveRoomSpecEntity();
            preLiveRoomSpecEntity.setTaobaoAccountNick(activeAccount.getNick());
            preLiveRoomSpecEntity.setLiveCoverImg("//gw.alicdn.com/tfscom/i2/O1CN01BKVJiU1Cl08UQKKbH_!!0-dgshop.jpg");
            preLiveRoomSpecEntity.setLiveCoverImg169("//gw.alicdn.com/tfscom/i4/O1CN01v11ZoD1Cl08S6maCv_!!0-dgshop.jpg");
            preLiveRoomSpecEntity.setLiveTitle("蜜蜡放漏了");
            preLiveRoomSpecEntity.setLiveIntro("关注直播");
            preLiveRoomSpecEntity.setLiveAppointmentTime(new Date(2020 - 1900, 9, 1, 12, 30, 0));
            preLiveRoomSpecEntity.setLiveChannelId(7);
            preLiveRoomSpecEntity.setLiveColumnId(123);
            preLiveRoomSpecEntity.setLiveLocation("中国");

            R r = taobaoApiService.createLiveRoom(preLiveRoomSpecEntity, activeAccount);

            ObjectMapper objectMapper = new ObjectMapper();

            System.out.println(objectMapper.writeValueAsString(r));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    void startLiveRoom() {
        try {
            TaobaoAccountEntity activeAccount = null;
            List<TaobaoAccountEntity> taobaoAccountEntities = taobaoAccountService.list();
            for (TaobaoAccountEntity taobaoAccountEntity : taobaoAccountEntities) {
                if (taobaoAccountEntity.getState() == TaobaoAccountState.Normal.getState() &&
                        taobaoAccountEntity.getNick().equalsIgnoreCase("zhaoxiangchi00")) {
                    activeAccount = taobaoAccountEntity;
                    System.out.println("选择用户：" + activeAccount.getNick());
                    break;
                }
            }
            if (activeAccount == null) {
                System.out.println("好不到西晶香");
                return;
            }

            R r = taobaoApiService.getLiveInfo("￥FIDI1DyXdOr￥", activeAccount);

            ObjectMapper objectMapper = new ObjectMapper();
            System.out.println(objectMapper.writeValueAsString(r));

            R r1 = taobaoApiService.getLiveDetail("267887980908");
            System.out.println(objectMapper.writeValueAsString(r1));

            LiveRoomEntity liveRoomEntity = (LiveRoomEntity) r.get("live_room");

            r = taobaoApiService.startLive(liveRoomEntity, activeAccount);

            System.out.println(objectMapper.writeValueAsString(r));

            r = taobaoApiService.stopLive(liveRoomEntity, activeAccount);

            System.out.println(objectMapper.writeValueAsString(r));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    void addProduct() {
        try {
            TaobaoAccountEntity activeAccount = null;
            List<TaobaoAccountEntity> taobaoAccountEntities = taobaoAccountService.list();
            for (TaobaoAccountEntity taobaoAccountEntity : taobaoAccountEntities) {
                if (taobaoAccountEntity.getState() == TaobaoAccountState.Normal.getState() &&
                        taobaoAccountEntity.getNick().equalsIgnoreCase("zhaoxiangchi00")) {
                    activeAccount = taobaoAccountEntity;
                    System.out.println("选择用户：" + activeAccount.getNick());
                    break;
                }
            }
            if (activeAccount == null) {
                System.out.println("好不到西晶香");
                return;
            }

            R r = taobaoApiService.getLiveInfo("￥FIDI1DyXdOr￥", activeAccount);

            ObjectMapper objectMapper = new ObjectMapper();
            System.out.println(objectMapper.writeValueAsString(r));

            LiveRoomEntity liveRoomEntity = (LiveRoomEntity) r.get("live_room");

            r = taobaoApiService.getProductItemInfo(activeAccount, "614611062242");

            System.out.println(objectMapper.writeValueAsString(r));

            ProductEntity productEntity = new ProductEntity();
            productEntity.setProductId((String) r.get("product_id"));
            productEntity.setPicurl((String) r.get("img_url"));
            productEntity.setTitle((String) r.get("title"));
            productEntity.setPrice((String) r.get("price"));

            r = taobaoApiService.publishProductWeb(liveRoomEntity, activeAccount, productEntity);

            System.out.println(objectMapper.writeValueAsString(r));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    void stopLiveRoom() {
        try {
            TaobaoAccountEntity activeAccount = null;
            List<TaobaoAccountEntity> taobaoAccountEntities = taobaoAccountService.list();
            for (TaobaoAccountEntity taobaoAccountEntity : taobaoAccountEntities) {
                if (taobaoAccountEntity.getState() == TaobaoAccountState.Normal.getState() &&
                        taobaoAccountEntity.getNick().equalsIgnoreCase("zhaoxiangchi00")) {
                    activeAccount = taobaoAccountEntity;
                    System.out.println("选择用户：" + activeAccount.getNick());
                    break;
                }
            }
            if (activeAccount == null) {
                System.out.println("好不到西晶香");
                return;
            }

            R r = taobaoApiService.getLiveInfo("￥FIDI1DyXdOr￥", activeAccount);

            ObjectMapper objectMapper = new ObjectMapper();
            System.out.println(objectMapper.writeValueAsString(r));

            LiveRoomEntity liveRoomEntity = (LiveRoomEntity) r.get("live_room");

            System.out.println(objectMapper.writeValueAsString(r));

            r = taobaoApiService.stopLive(liveRoomEntity, activeAccount);

            System.out.println(objectMapper.writeValueAsString(r));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    void getLiveProducts() {
        try {
            TaobaoAccountEntity activeAccount = null;
            List<TaobaoAccountEntity> taobaoAccountEntities = taobaoAccountService.list();
            for (TaobaoAccountEntity taobaoAccountEntity : taobaoAccountEntities) {
                if (taobaoAccountEntity.getState() == TaobaoAccountState.Normal.getState() &&
                        taobaoAccountEntity.getNick().equalsIgnoreCase("zhaoxiangchi00")) {
                    activeAccount = taobaoAccountEntity;
                    System.out.println("选择用户：" + activeAccount.getNick());
                    break;
                }
            }
            if (activeAccount == null) {
                System.out.println("好不到西晶香");
                return;
            }

            taobaoApiService.getH5Token(activeAccount);

            R r = taobaoApiService.getLiveList(activeAccount, 1, 20);

            List<LiveRoomEntity> liveRoomEntities = (List<LiveRoomEntity>) r.get("live_rooms");

            ObjectMapper objectMapper = new ObjectMapper();
            System.out.println(objectMapper.writeValueAsString(r));

            LiveRoomEntity activeLiveRoom = null;
            for (LiveRoomEntity liveRoomEntity : liveRoomEntities) {
                r = taobaoApiService.getLiveProducts(liveRoomEntity, activeAccount);

                System.out.println(objectMapper.writeValueAsString(r));

                if (liveRoomEntity.getLiveState() == LiveRoomState.Published.getState()) {
                    activeLiveRoom = liveRoomEntity;
                }
            }

            if (activeLiveRoom != null) {
                r = taobaoApiService.getProductItemInfo(activeAccount, "613148684126");

                System.out.println(objectMapper.writeValueAsString(r));

                ProductEntity productEntity = new ProductEntity();
                productEntity.setProductId((String) r.get("product_id"));
                productEntity.setPicurl((String) r.get("img_url"));
                productEntity.setTitle((String) r.get("title"));
                productEntity.setPrice((String) r.get("price"));

                r = taobaoApiService.publishProductWeb(activeLiveRoom, activeAccount, productEntity);

                System.out.println(objectMapper.writeValueAsString(r));

                r = taobaoApiService.openProduct(activeAccount, productEntity.getProductId());
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    void deleteLiveRooms() {
        try {
            TaobaoAccountEntity activeAccount = null;
            List<TaobaoAccountEntity> taobaoAccountEntities = taobaoAccountService.list();
            for (TaobaoAccountEntity taobaoAccountEntity : taobaoAccountEntities) {
                if (taobaoAccountEntity.getState() == TaobaoAccountState.Normal.getState() &&
                        taobaoAccountEntity.getNick().equalsIgnoreCase("zhaoxiangchi00")) {
                    activeAccount = taobaoAccountEntity;
                    System.out.println("选择用户：" + activeAccount.getNick());
                    break;
                }
            }
            if (activeAccount == null) {
                System.out.println("好不到西晶香");
                return;
            }

            taobaoApiService.getH5Token(activeAccount);

            R r = taobaoApiService.getLiveList(activeAccount, 1, 20);

            List<LiveRoomEntity> liveRoomEntities = (List<LiveRoomEntity>) r.get("live_rooms");

            ObjectMapper objectMapper = new ObjectMapper();
            System.out.println(objectMapper.writeValueAsString(r));

            LiveRoomEntity activeLiveRoom = null;
            for (LiveRoomEntity liveRoomEntity : liveRoomEntities) {
                r = taobaoApiService.getLiveProducts(liveRoomEntity, activeAccount);

                if (liveRoomEntity.getLiveState() == LiveRoomState.Stopped.getState()) {
                    r = taobaoApiService.deleteLive(liveRoomEntity, activeAccount);

                    System.out.println(objectMapper.writeValueAsString(r));
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
