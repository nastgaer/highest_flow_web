package highest.flow.taobaolive;

import com.fasterxml.jackson.databind.ObjectMapper;
import highest.flow.taobaolive.common.defines.ErrorCodes;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.taobao.defines.TaobaoAccountState;
import highest.flow.taobaolive.taobao.entity.LiveRoom;
import highest.flow.taobaolive.taobao.entity.Product;
import highest.flow.taobaolive.taobao.entity.TaobaoAccount;
import highest.flow.taobaolive.taobao.service.TaobaoAccountService;
import highest.flow.taobaolive.taobao.service.TaobaoApiService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest(classes = TaobaoliveApplication.class, args = "test")
public class TaobaoAccountTests {

    @Autowired
    private TaobaoApiService taobaoApiService;

    @Autowired
    private TaobaoAccountService taobaoAccountService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    void getNewDeviceId() {
        List<TaobaoAccount> taobaoAccounts = taobaoAccountService.list();
        for (TaobaoAccount taobaoAccount : taobaoAccounts) {
            if (taobaoAccount.getState() == TaobaoAccountState.Normal.getState()) {
                R r = taobaoApiService.getH5Token(taobaoAccount);
                if (r.getCode() == ErrorCodes.SUCCESS) {
                    System.out.println(taobaoAccount.getNick() + ": H5Token成功");
                } else {
                    System.out.println(taobaoAccount.getNick() + ": H5Token失败，" + r.getMsg());
                }

                r = taobaoApiService.getUmtidToken();
                if (r.getCode() == ErrorCodes.SUCCESS) {
                    System.out.println(taobaoAccount.getNick() + ": getUmtidToken成功，" + r.get("umtid").toString());
                } else {
                    System.out.println(taobaoAccount.getNick() + ": getUmtidToken失败，" + r.getMsg());
                }

                r = taobaoApiService.getNewDeviceId(taobaoAccount);
                if (r.getCode() == ErrorCodes.SUCCESS) {
                    System.out.println(taobaoAccount.getNick() + ": 获取机器码成功，" + r.get("device_id"));
                } else {
                    System.out.println(taobaoAccount.getNick() + ": 获取机器码失败，" + r.getMsg());
                }
            }
        }
    }

    @Test
    void parseTaoCode() {
        String taocode = "￥EwwQ1Esktcm￥";

        try {
            ObjectMapper objectMapper = new ObjectMapper();

            R r = taobaoApiService.parseTaoCode(taocode);
            System.out.println(objectMapper.writeValueAsString(r));

            LiveRoom liveRoom = new LiveRoom();
            liveRoom.setLiveId((String) r.get("liveId"));
            liveRoom.setCreatorId((String) r.get("creatorId"));
            liveRoom.setTalentLiveUrl((String) r.get("talentLiveUrl"));

            String liveId = (String) r.get("liveId");
            r = taobaoApiService.getLiveDetail(liveId);
            System.out.println(objectMapper.writeValueAsString(r));

            liveRoom.setAccountId((String) r.get("accountId"));
            liveRoom.setAccountName((String) r.get("accountName"));
            liveRoom.setFansNum((int) r.get("fansNum"));
            liveRoom.setTopic((String) r.get("topic"));
            liveRoom.setViewCount((int) r.get("viewCount"));
            liveRoom.setPraiseCount((int) r.get("praiseCount"));
            liveRoom.setOnlineCount((int) r.get("onlineCount"));
            liveRoom.setCoverImg((String) r.get("coverImg"));
            liveRoom.setCoverImg169((String) r.get("coverImg169"));
            liveRoom.setTitle((String) r.get("title"));
            liveRoom.setIntro((String) r.get("intro"));
            liveRoom.setChannelId((int) r.get("channelId"));
            liveRoom.setColumnId((int) r.get("columnId"));
            liveRoom.setLocation((String) r.get("location"));

            TaobaoAccount activeAccount = null;
            List<TaobaoAccount> taobaoAccounts = taobaoAccountService.list();
            for (TaobaoAccount taobaoAccount : taobaoAccounts) {
                if (taobaoAccount.getState() == TaobaoAccountState.Normal.getState()) {
                    activeAccount = taobaoAccount;
                    break;
                }
            }

            if (activeAccount != null) {
                taobaoApiService.getH5Token(activeAccount);
                r = taobaoApiService.getLiveEntry(liveRoom, activeAccount);
                System.out.println(objectMapper.writeValueAsString(r));

                liveRoom.setScopeId((String) r.get("scopeId"));
                liveRoom.setSubScopeId((String) r.get("subScopeId"));

                r = taobaoApiService.getLiveProducts(liveRoom, activeAccount);
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

            LiveRoom liveRoom = new LiveRoom();
            liveRoom.setLiveId((String) r.get("liveId"));
            liveRoom.setCreatorId((String) r.get("creatorId"));
            liveRoom.setTalentLiveUrl((String) r.get("talentLiveUrl"));

            String liveId = (String) r.get("liveId");
            r = taobaoApiService.getLiveDetail(liveId);
            System.out.println(objectMapper.writeValueAsString(r));

            liveRoom.setAccountId((String) r.get("accountId"));
            liveRoom.setAccountName((String) r.get("accountName"));
            liveRoom.setFansNum((int) r.get("fansNum"));
            liveRoom.setTopic((String) r.get("topic"));
            liveRoom.setViewCount((int) r.get("viewCount"));
            liveRoom.setPraiseCount((int) r.get("praiseCount"));
            liveRoom.setOnlineCount((int) r.get("onlineCount"));
            liveRoom.setCoverImg((String) r.get("coverImg"));
            liveRoom.setCoverImg169((String) r.get("coverImg169"));
            liveRoom.setTitle((String) r.get("title"));
            liveRoom.setIntro((String) r.get("intro"));
            liveRoom.setChannelId((int) r.get("channelId"));
            liveRoom.setColumnId((int) r.get("columnId"));
            liveRoom.setLocation((String) r.get("location"));

            List<TaobaoAccount> taobaoAccounts = taobaoAccountService.list();

            TaobaoAccount activeAccount = null;
            for (TaobaoAccount taobaoAccount : taobaoAccounts) {
                if (taobaoAccount.getState() == TaobaoAccountState.Normal.getState()) {
                    activeAccount = taobaoAccount;
                    break;
                }
            }

            taobaoApiService.getH5Token(activeAccount);
            taobaoApiService.getLiveEntry(liveRoom, activeAccount);
            r = taobaoApiService.getLiveProducts(liveRoom, activeAccount);
            List<Product> products = (List<Product>) r.get("products");

            for (TaobaoAccount taobaoAccount : taobaoAccounts) {
                if (taobaoAccount.getState() == TaobaoAccountState.Normal.getState()) {
                    taobaoApiService.getH5Token(taobaoAccount);

                    r = taobaoApiService.taskFollow(liveRoom, taobaoAccount);
                    System.out.println(objectMapper.writeValueAsString(r));

                    r = taobaoApiService.taskBuy(liveRoom, taobaoAccount, products.get(0).getProductId());
                    System.out.println(objectMapper.writeValueAsString(r));

                    r = taobaoApiService.taskStay(liveRoom, taobaoAccount, 60);
                    System.out.println(objectMapper.writeValueAsString(r));
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
