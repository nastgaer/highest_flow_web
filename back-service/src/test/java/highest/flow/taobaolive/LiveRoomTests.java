package highest.flow.taobaolive;

import com.fasterxml.jackson.databind.ObjectMapper;
import highest.flow.taobaolive.common.config.Config;
import highest.flow.taobaolive.common.http.HttpHelper;
import highest.flow.taobaolive.common.http.Request;
import highest.flow.taobaolive.common.http.ResponseType;
import highest.flow.taobaolive.common.http.SiteConfig;
import highest.flow.taobaolive.common.http.httpclient.response.Response;
import highest.flow.taobaolive.common.utils.R;
import org.apache.http.entity.StringEntity;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LiveRoomTests extends BaseTests {

    @Test
    public void addRoom() {
        try {
            contextLoads();

            Map<String, Object> serviceMap = new HashMap<>();
            serviceMap.put("start_date", "2020-07-10");
            serviceMap.put("days", 30);

            Map<String, Object> liveSpecMap = new HashMap<>();
            liveSpecMap.put("live_cover_img", "//gw.alicdn.com/tfscom/i2/O1CN01BKVJiU1Cl08UQKKbH_!!0-dgshop.jpg");
            liveSpecMap.put("live_cover_img169", "//gw.alicdn.com/tfscom/i4/O1CN01v11ZoD1Cl08S6maCv_!!0-dgshop.jpg");
            liveSpecMap.put("live_title", "新主播来了");
            liveSpecMap.put("live_intro", "多多关心我啊");
            liveSpecMap.put("live_start_time", "2020-07-10 10:00:00");
            liveSpecMap.put("live_end_time", "2020-08-10 10:00:00");
            liveSpecMap.put("live_channel_id", 7);
            liveSpecMap.put("live_column_id", 123);
            liveSpecMap.put("live_location", "中国");
            liveSpecMap.put("hot_product_url", "https://item.taobao.com/item.htm?id=616276032626");

            List<Map> liveSpecsMap = new ArrayList<>();
            liveSpecsMap.add(liveSpecMap);

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("taobao_account_nick", "tb150494815");
            paramMap.put("comment", "");
            paramMap.put("service", serviceMap);
            paramMap.put("live_specs", liveSpecsMap);

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(paramMap);

            String url = host + "/v1.0/live/add_room";

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
    public void listRooms() {
        try {
            contextLoads();

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("page_no", 1);
            paramMap.put("page_size", 20);
            paramMap.put("keyword", "");

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(paramMap);

            String url = host + "/v1.0/live/list";

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
}
