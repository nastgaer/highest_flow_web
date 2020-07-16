package highest.flow.taobaolive;

import com.fasterxml.jackson.databind.ObjectMapper;
import highest.flow.taobaolive.common.http.HttpHelper;
import highest.flow.taobaolive.common.http.Request;
import highest.flow.taobaolive.common.http.ResponseType;
import highest.flow.taobaolive.common.http.SiteConfig;
import highest.flow.taobaolive.common.http.httpclient.response.Response;
import org.apache.http.entity.StringEntity;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LiveTemplateTests extends BaseTests {

    @Test
    public void listTemplates() {
        try {
            contextLoads();

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("page_no", 1);
            paramMap.put("page_size", 20);
            paramMap.put("keyword", "");

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(paramMap);

            String url = "http://localhost:8080/v1.0/live/template/list";

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
    public void addTemplate() {
        try {
            contextLoads();

            Map<String, Object> blocksMap = new HashMap<>();
            blocksMap.put("live_kind", 0);
            blocksMap.put("live_channel_id", 7);
            blocksMap.put("live_column_id", 123);
            blocksMap.put("psc_channel_id", 1);
            blocksMap.put("psc_category_id", 2);
            blocksMap.put("psc_start_price", 0);
            blocksMap.put("psc_end_price", 0);
            blocksMap.put("psc_min_sales", 0);
            blocksMap.put("psc_product_count", 350);
            blocksMap.put("psc_is_tmall", false);
            blocksMap.put("psc_sort_kind", 0);

            List<Map> blocksList = new ArrayList<>();
            blocksList.add(blocksMap);

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("template_name", "爆发模板");
            paramMap.put("template_kind", 0);
            paramMap.put("blocks", blocksList);

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(paramMap);

            String url = "http://localhost:8080/v1.0/live/template/add";

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
    public void getTemplate() {
        try {
            contextLoads();

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("template_name", "爆发模板");

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(paramMap);

            String url = "http://localhost:8080/v1.0/live/template/get";

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
