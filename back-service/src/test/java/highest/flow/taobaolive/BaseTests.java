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

import java.util.HashMap;
import java.util.Map;

@SpringBootTest(classes = TaobaoliveApplication.class, args = "test")
public class BaseTests {

    protected String accessToken = "";

    @Test
    public void contextLoads() {
        try {
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("member_name", Config.ADMINISTRATOR);
            paramMap.put("password", Config.ADMINISTRATOR);

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(paramMap);

            String url = "http://localhost:8080/sys/login";

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setContentType("application/json"),
                    new Request("POST", url, ResponseType.TEXT)
                            .setEntity(new StringEntity(json)));

            System.out.println(response.getResult());

            R r = objectMapper.readValue(response.getResult(), R.class);

            System.out.println(r.get("access_token"));

            this.accessToken = String.valueOf(r.get("access_token"));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


}
