package highest.flow.taobaolive;

import com.fasterxml.jackson.databind.ObjectMapper;
import highest.flow.taobaolive.common.http.HttpHelper;
import highest.flow.taobaolive.common.http.Request;
import highest.flow.taobaolive.common.http.ResponseType;
import highest.flow.taobaolive.common.http.SiteConfig;
import highest.flow.taobaolive.common.http.httpclient.response.Response;
import org.apache.http.entity.StringEntity;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest(classes = TaobaoliveApplication.class, args = "test")
public class SystemTests extends BaseTests {

    @Test
    void logs() {
        try {
            contextLoads();

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("page_no", 1);
            paramMap.put("page_size", 20);

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(paramMap);

            String url = host + "/v1.0/sys/logs";

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
    void dashboard() {
        try {
            contextLoads();

            String url = host + "/v1.0/sys/dashboard/tbacc";

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setContentType("application/json")
                            .addHeader("access_token", accessToken),
                    new Request("POST", url, ResponseType.TEXT));

            System.out.println(response.getResult());

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("start_date", "2020-07-10");
            paramMap.put("end_date", "2020-08-10");

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(paramMap);

            url = host + "/v1.0/sys/dashboard/ranking";

            response = HttpHelper.execute(
                    new SiteConfig()
                            .setContentType("application/json")
                            .addHeader("access_token", accessToken),
                    new Request("POST", url, ResponseType.TEXT)
                            .setEntity(new StringEntity(json)));

            System.out.println(response.getResult());

            url = host + "/v1.0/sys/dashboard/live";

            response = HttpHelper.execute(
                    new SiteConfig()
                            .setContentType("application/json")
                            .addHeader("access_token", accessToken),
                    new Request("POST", url, ResponseType.TEXT));

            System.out.println(response.getResult());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
