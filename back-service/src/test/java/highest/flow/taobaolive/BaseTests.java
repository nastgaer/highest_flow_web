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

    public String host = "http://localhost:8080";
    public String username = "administrator";
    public String password = "administrator";

//    public String host = "http://119.45.148.200:8080/highest";
//    public String username = "administrator";
//    public String password = "1234qwer!@#$qwer";

    @Test
    public void contextLoads() {
        try {
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("member_name", username);
            paramMap.put("password", password);

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(paramMap);

            String url = host + "/v1.0/sys/login";

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
