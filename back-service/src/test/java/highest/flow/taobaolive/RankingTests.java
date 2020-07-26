package highest.flow.taobaolive;

import com.fasterxml.jackson.databind.ObjectMapper;
import highest.flow.taobaolive.common.config.Config;
import highest.flow.taobaolive.common.http.HttpHelper;
import highest.flow.taobaolive.common.http.Request;
import highest.flow.taobaolive.common.http.ResponseType;
import highest.flow.taobaolive.common.http.SiteConfig;
import highest.flow.taobaolive.common.http.httpclient.response.Response;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.taobao.defines.RankingEntityState;
import highest.flow.taobaolive.taobao.defines.TaobaoAccountState;
import highest.flow.taobaolive.taobao.entity.LiveRoomEntity;
import highest.flow.taobaolive.taobao.entity.ProductEntity;
import highest.flow.taobaolive.taobao.entity.RankingEntity;
import highest.flow.taobaolive.taobao.entity.TaobaoAccountEntity;
import highest.flow.taobaolive.taobao.service.TaobaoAccountService;
import highest.flow.taobaolive.taobao.service.TaobaoApiService;
import org.apache.commons.lang.time.DateUtils;
import org.apache.http.entity.StringEntity;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.SimpleDateFormat;
import java.util.*;

public class RankingTests extends BaseTests {

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

            TaobaoAccountEntity activeAccount = null;
            List<TaobaoAccountEntity> taobaoAccountEntities = taobaoAccountService.list();
            for (TaobaoAccountEntity taobaoAccountEntity : taobaoAccountEntities) {
                if (taobaoAccountEntity.getState() == TaobaoAccountState.Normal.getState()) {
                    activeAccount = taobaoAccountEntity;
                    break;
                }
            }

            R r = taobaoApiService.getLiveInfo(taocode, null);
            System.out.println(objectMapper.writeValueAsString(r));

            LiveRoomEntity liveRoomEntity = (LiveRoomEntity) r.get("live_room");

            if (activeAccount != null) {
                r = taobaoApiService.getLiveProducts(liveRoomEntity, activeAccount);
                System.out.println(objectMapper.writeValueAsString(r));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    void getLiveInfo() {
        try {
            contextLoads();

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("live_id", "273501295556");

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(paramMap);

            String url = "http://localhost:8080/v1.0/live/get_live_info";

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setContentType("application/json")
                            .addHeader("access_token", accessToken),
                    new Request("POST", url, ResponseType.TEXT)
                            .setEntity(new StringEntity(json)));

            System.out.println(response.getResult());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    void rankingApi() {
        String taocode = "￥EwwQ1Esktcm￥";

        try {
            ObjectMapper objectMapper = new ObjectMapper();

            TaobaoAccountEntity activeAccount = null;
            List<TaobaoAccountEntity> taobaoAccountEntities = taobaoAccountService.list();
            for (TaobaoAccountEntity taobaoAccountEntity : taobaoAccountEntities) {
                if (taobaoAccountEntity.getState() == TaobaoAccountState.Normal.getState()) {
                    activeAccount = taobaoAccountEntity;
                    break;
                }
            }

            R r = taobaoApiService.getLiveInfo(taocode, null);
            System.out.println(objectMapper.writeValueAsString(r));

            LiveRoomEntity liveRoomEntity = (LiveRoomEntity) r.get("live_room");
            r = taobaoApiService.getLiveProducts(liveRoomEntity, activeAccount);
            System.out.println(objectMapper.writeValueAsString(r));

            List<ProductEntity> productEntities = (List<ProductEntity>) r.get("products");

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

    @Test
    void addRankingTask() {
        try {
            contextLoads();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date();
            date = DateUtils.addHours(date, 1);

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("taocode", "￥OoG21BgDahH￥");
            paramMap.put("target_score", "100000");
            paramMap.put("double_buy", false);
            paramMap.put("start_time", "2020-07-17 22:30:00");

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(paramMap);

            String url = "http://localhost:8080/v1.0/ranking/add_task";

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setContentType("application/json")
                            .addHeader("access_token", accessToken),
                    new Request("POST", url, ResponseType.TEXT)
                            .setEntity(new StringEntity(json, "UTF-8")));

            System.out.println(response.getResult());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    void startTodaysTask() {
        try {
            contextLoads();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("current_date", sdf.format(new Date()));

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(paramMap);

            String url = "http://localhost:8080/v1.0/ranking/todays";

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setContentType("application/json")
                            .addHeader("access_token", accessToken),
                    new Request("POST", url, ResponseType.TEXT)
                            .setEntity(new StringEntity(json)));

            System.out.println(response.getResult());

            R r = objectMapper.readValue(response.getResult(), R.class);

            List<Map<String, Object>> rankingEntities = (List<Map<String, Object>>) r.get("ranking");

            int taskId = -1;
            for (Map<String, Object> map : rankingEntities) {
                int state = (int) map.get("state");
                if (state == RankingEntityState.Waiting.getState()) {
                    taskId = (int) map.get("id");
                    break;
                }
            }

            if (taskId < 0) {
                return;
            }

            // Start first ranking

            url = "http://localhost:8080/v1.0/ranking/start_task";

            paramMap.clear();
            paramMap.put("task_id", taskId);

            json = objectMapper.writeValueAsString(paramMap);

            response = HttpHelper.execute(
                    new SiteConfig()
                            .setContentType("application/json")
                            .addHeader("access_token", accessToken),
                    new Request("POST", url, ResponseType.TEXT)
                            .setEntity(new StringEntity(json)));

            System.out.println(response.getResult());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    void stopTask() {
        try {
            contextLoads();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("current_date", sdf.format(new Date()));

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(paramMap);

            String url = "http://localhost:8080/v1.0/ranking/todays";

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setContentType("application/json")
                            .addHeader("access_token", accessToken),
                    new Request("POST", url, ResponseType.TEXT)
                            .setEntity(new StringEntity(json)));

            System.out.println(response.getResult());

            R r = objectMapper.readValue(response.getResult(), R.class);

            List<Map<String, Object>> rankingEntities = (List<Map<String, Object>>) r.get("ranking");

            int taskId = -1;
            for (Map<String, Object> map : rankingEntities) {
                int state = (int) map.get("state");
                if (state == RankingEntityState.Running.getState()) {
                    taskId = (int) map.get("id");
                    break;
                }
            }

            if (taskId < 0) {
                return;
            }

            // Start first ranking

            url = "http://localhost:http://localhost:8080/v1.0//ranking/stop_task";

            paramMap.clear();
            paramMap.put("task_id", taskId);

            json = objectMapper.writeValueAsString(paramMap);

            response = HttpHelper.execute(
                    new SiteConfig()
                            .setContentType("application/json")
                            .addHeader("access_token", accessToken),
                    new Request("POST", url, ResponseType.TEXT)
                            .setEntity(new StringEntity(json)));

            System.out.println(response.getResult());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    void logs() {
        try {
            contextLoads();

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("page_no", 1);
            paramMap.put("page_size", 20);

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(paramMap);

            String url = "http://localhost:8080/v1.0/ranking/logs";

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setContentType("application/json")
                            .addHeader("access_token", accessToken),
                    new Request("POST", url, ResponseType.TEXT)
                            .setEntity(new StringEntity(json)));

            System.out.println(response.getResult());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
