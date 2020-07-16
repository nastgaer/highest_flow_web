package highest.flow.taobaolive;

import com.fasterxml.jackson.databind.ObjectMapper;
import highest.flow.taobaolive.common.config.Config;
import highest.flow.taobaolive.common.http.HttpHelper;
import highest.flow.taobaolive.common.http.Request;
import highest.flow.taobaolive.common.http.ResponseType;
import highest.flow.taobaolive.common.http.SiteConfig;
import highest.flow.taobaolive.common.http.httpclient.response.Response;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.sys.service.MemberRoleService;
import highest.flow.taobaolive.sys.service.MemberService;
import org.apache.http.entity.StringEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest(classes = TaobaoliveApplication.class, args = "test")
public class MembersTest extends BaseTests {

    @Test
    void register() {
        try {
            contextLoads();

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("member_name", "user");
            paramMap.put("password", "user");
            paramMap.put("mobile", "123456789");
            paramMap.put("comment", "user");
            List<String> role = new ArrayList<>();
            role.add("tb_accounts");
            role.add("ranking");
            paramMap.put("role", role);

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(paramMap);

            String url = "http://localhost:http://localhost:8080/v1.0//sys/register";

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
    void list() {
        try {
            contextLoads();

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("page_no", "1");
            paramMap.put("page_size", "30");
            paramMap.put("keyword", "");

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(paramMap);

            String url = "http://localhost:http://localhost:8080/v1.0//sys/list";

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
    void update() {
        try {
            contextLoads();

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("id", 2);
            paramMap.put("member_name", "user");
            paramMap.put("password", "password");
            paramMap.put("mobile", "123456789");
            paramMap.put("comment", "user_modified");
            List<String> role = new ArrayList<>();
            role.add("members");
            role.add("ranking");
            paramMap.put("role", role);

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(paramMap);

            String url = "http://localhost:http://localhost:8080/v1.0//sys/update";

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
    void batchDelete() {
        try {
            contextLoads();

            List<String> ids = new ArrayList<>();
            ids.add("2");
            ids.add("3");

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("ids", ids);

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(paramMap);

            String url = "http://localhost:http://localhost:8080/v1.0//sys/batch_delete";

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
    void logs() {
        try {
            contextLoads();

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("page_no", 1);
            paramMap.put("page_size", 20);

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(paramMap);

            String url = "http://localhost:8080/v1.0/sys/logs";

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
