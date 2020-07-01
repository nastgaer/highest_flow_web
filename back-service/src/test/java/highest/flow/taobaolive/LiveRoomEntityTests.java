package highest.flow.taobaolive;

import com.fasterxml.jackson.databind.ObjectMapper;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.taobao.defines.TaobaoAccountState;
import highest.flow.taobaolive.taobao.entity.PreLiveRoomSpecEntity;
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
            preLiveRoomSpecEntity.setMemberId(0);
            preLiveRoomSpecEntity.setCoverImg("//gw.alicdn.com/tfscom/i2/O1CN01BKVJiU1Cl08UQKKbH_!!0-dgshop.jpg");
            preLiveRoomSpecEntity.setCoverImg169("//gw.alicdn.com/tfscom/i4/O1CN01v11ZoD1Cl08S6maCv_!!0-dgshop.jpg");
            preLiveRoomSpecEntity.setTitle("蜜蜡放漏了");
            preLiveRoomSpecEntity.setIntro("关注直播");
            preLiveRoomSpecEntity.setStartTime(new Date(2020, 7, 1, 12, 30, 0));
            preLiveRoomSpecEntity.setEndTime(new Date(2020, 8, 1, 12, 30, 0));
            preLiveRoomSpecEntity.setChannelId(7);
            preLiveRoomSpecEntity.setColumnId(123);
            preLiveRoomSpecEntity.setLocation("中国");

            R r = taobaoApiService.createLiveRoom(preLiveRoomSpecEntity, activeAccount);


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
