package highest.flow.taobaolive;

import com.fasterxml.jackson.databind.ObjectMapper;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.taobao.defines.TaobaoAccountState;
import highest.flow.taobaolive.taobao.entity.LiveRoomEntity;
import highest.flow.taobaolive.taobao.entity.ProductEntity;
import highest.flow.taobaolive.taobao.entity.TaobaoAccountEntity;
import highest.flow.taobaolive.taobao.service.TaobaoAccountService;
import highest.flow.taobaolive.taobao.service.TaobaoApiService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest(classes = TaobaoliveApplication.class, args = "test")
public class RankingTests {

    @Autowired
    private TaobaoApiService taobaoApiService;

    @Autowired
    private TaobaoAccountService taobaoAccountService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    void parseTaoCode() {
        String taocode = "￥EwwQ1Esktcm￥";

        try {
            ObjectMapper objectMapper = new ObjectMapper();

            R r = taobaoApiService.parseTaoCode(taocode);
            System.out.println(objectMapper.writeValueAsString(r));

            LiveRoomEntity liveRoomEntity = new LiveRoomEntity();
            liveRoomEntity.setLiveId((String) r.get("liveId"));
            liveRoomEntity.setCreatorId((String) r.get("creatorId"));
            liveRoomEntity.setTalentLiveUrl((String) r.get("talentLiveUrl"));

            String liveId = (String) r.get("liveId");
            r = taobaoApiService.getLiveDetail(liveId);
            System.out.println(objectMapper.writeValueAsString(r));

            liveRoomEntity.setAccountId((String) r.get("accountId"));
            liveRoomEntity.setAccountName((String) r.get("accountName"));
            liveRoomEntity.setFansNum((int) r.get("fansNum"));
            liveRoomEntity.setTopic((String) r.get("topic"));
            liveRoomEntity.setViewCount((int) r.get("viewCount"));
            liveRoomEntity.setPraiseCount((int) r.get("praiseCount"));
            liveRoomEntity.setOnlineCount((int) r.get("onlineCount"));
            liveRoomEntity.setCoverImg((String) r.get("coverImg"));
            liveRoomEntity.setCoverImg169((String) r.get("coverImg169"));
            liveRoomEntity.setTitle((String) r.get("title"));
            liveRoomEntity.setIntro((String) r.get("intro"));
            liveRoomEntity.setChannelId((int) r.get("channelId"));
            liveRoomEntity.setColumnId((int) r.get("columnId"));
            liveRoomEntity.setLocation((String) r.get("location"));

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
                r = taobaoApiService.getLiveEntry(liveRoomEntity, activeAccount);
                System.out.println(objectMapper.writeValueAsString(r));

                liveRoomEntity.setScopeId((String) r.get("scopeId"));
                liveRoomEntity.setSubScopeId((String) r.get("subScopeId"));

                r = taobaoApiService.getLiveProducts(liveRoomEntity, activeAccount);
                System.out.println(objectMapper.writeValueAsString(r));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    void ranking() {
        String taocode = "￥EwwQ1Esktcm￥";

        try {
            ObjectMapper objectMapper = new ObjectMapper();

            R r = taobaoApiService.parseTaoCode(taocode);
            System.out.println(objectMapper.writeValueAsString(r));

            LiveRoomEntity liveRoomEntity = new LiveRoomEntity();
            liveRoomEntity.setLiveId((String) r.get("liveId"));
            liveRoomEntity.setCreatorId((String) r.get("creatorId"));
            liveRoomEntity.setTalentLiveUrl((String) r.get("talentLiveUrl"));

            String liveId = (String) r.get("liveId");
            r = taobaoApiService.getLiveDetail(liveId);
            System.out.println(objectMapper.writeValueAsString(r));

            liveRoomEntity.setAccountId((String) r.get("accountId"));
            liveRoomEntity.setAccountName((String) r.get("accountName"));
            liveRoomEntity.setFansNum((int) r.get("fansNum"));
            liveRoomEntity.setTopic((String) r.get("topic"));
            liveRoomEntity.setViewCount((int) r.get("viewCount"));
            liveRoomEntity.setPraiseCount((int) r.get("praiseCount"));
            liveRoomEntity.setOnlineCount((int) r.get("onlineCount"));
            liveRoomEntity.setCoverImg((String) r.get("coverImg"));
            liveRoomEntity.setCoverImg169((String) r.get("coverImg169"));
            liveRoomEntity.setTitle((String) r.get("title"));
            liveRoomEntity.setIntro((String) r.get("intro"));
            liveRoomEntity.setChannelId((int) r.get("channelId"));
            liveRoomEntity.setColumnId((int) r.get("columnId"));
            liveRoomEntity.setLocation((String) r.get("location"));

            List<TaobaoAccountEntity> taobaoAccountEntities = taobaoAccountService.list();

            TaobaoAccountEntity activeAccount = null;
            for (TaobaoAccountEntity taobaoAccountEntity : taobaoAccountEntities) {
                if (taobaoAccountEntity.getState() == TaobaoAccountState.Normal.getState()) {
                    activeAccount = taobaoAccountEntity;
                    break;
                }
            }

            taobaoApiService.getH5Token(activeAccount);
            taobaoApiService.getLiveEntry(liveRoomEntity, activeAccount);
            r = taobaoApiService.getLiveProducts(liveRoomEntity, activeAccount);
            List<ProductEntity> productEntities = (List<ProductEntity>) r.get("productEntities");

            for (TaobaoAccountEntity taobaoAccountEntity : taobaoAccountEntities) {
                if (taobaoAccountEntity.getState() == TaobaoAccountState.Normal.getState()) {
                    taobaoApiService.getH5Token(taobaoAccountEntity);

                    r = taobaoApiService.taskFollow(liveRoomEntity, taobaoAccountEntity);
                    System.out.println(objectMapper.writeValueAsString(r));

                    r = taobaoApiService.taskBuy(liveRoomEntity, taobaoAccountEntity, productEntities.get(0).getProductId());
                    System.out.println(objectMapper.writeValueAsString(r));

                    r = taobaoApiService.taskStay(liveRoomEntity, taobaoAccountEntity, 60);
                    System.out.println(objectMapper.writeValueAsString(r));
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
