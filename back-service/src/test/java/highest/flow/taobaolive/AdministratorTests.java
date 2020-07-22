package highest.flow.taobaolive;

import com.fasterxml.jackson.databind.ObjectMapper;
import highest.flow.taobaolive.common.config.Config;
import highest.flow.taobaolive.common.http.HttpHelper;
import highest.flow.taobaolive.common.http.Request;
import highest.flow.taobaolive.common.http.ResponseType;
import highest.flow.taobaolive.common.http.SiteConfig;
import highest.flow.taobaolive.common.http.httpclient.response.Response;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.sys.defines.MemberLevel;
import highest.flow.taobaolive.sys.defines.MemberState;
import highest.flow.taobaolive.sys.entity.SysMemberRole;
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
public class AdministratorTests {

    @Autowired
    private MemberRoleService memberRoleService;

    @Autowired
    private MemberService memberService;

    @Test
    void addAdministrator() {
        try {
            List<SysMemberRole> memberRoles = memberRoleService.list();

            List<String> roles = new ArrayList<>();

            for (SysMemberRole sysMemberRole : memberRoles) {
                roles.add(sysMemberRole.getName());
            }

            memberService.register(Config.ADMINISTRATOR,
                    Config.ADMINISTRATOR,
                    "111-1111-1111",
                    "Administrator",
                    roles,
                    MemberLevel.Administrator.getLevel(),
                    MemberState.Normal.getState());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    void loginLogoutAdministrator() {
        try {
            /**
             * Login
             */
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("member_name", Config.ADMINISTRATOR);
            paramMap.put("password", Config.ADMINISTRATOR);

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(paramMap);

            String url = "http://localhost:8080/v1.0/sys/login";

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setContentType("application/json"),
                    new Request("POST", url, ResponseType.TEXT)
                            .setEntity(new StringEntity(json)));

            System.out.println(response.getResult());

            R r = objectMapper.readValue(response.getResult(), R.class);

            System.out.println(r.get("access_token"));

            String accessToken = String.valueOf(r.get("access_token"));

            /**
             * Logout
             */
            paramMap.clear();
            paramMap.put("member_name", Config.ADMINISTRATOR);

            json = objectMapper.writeValueAsString(paramMap);

            url = "http://localhost:8080/v1.0/sys/logout";

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
}
