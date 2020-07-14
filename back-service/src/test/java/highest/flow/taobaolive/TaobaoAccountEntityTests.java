package highest.flow.taobaolive;

import com.fasterxml.jackson.databind.ObjectMapper;
import highest.flow.taobaolive.common.config.Config;
import highest.flow.taobaolive.common.defines.ErrorCodes;
import highest.flow.taobaolive.common.http.HttpHelper;
import highest.flow.taobaolive.common.http.Request;
import highest.flow.taobaolive.common.http.ResponseType;
import highest.flow.taobaolive.common.http.SiteConfig;
import highest.flow.taobaolive.common.http.httpclient.response.Response;
import highest.flow.taobaolive.common.utils.HFStringUtils;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.taobao.defines.TaobaoAccountState;
import highest.flow.taobaolive.taobao.entity.TaobaoAccountEntity;
import highest.flow.taobaolive.taobao.service.TaobaoAccountService;
import highest.flow.taobaolive.taobao.service.TaobaoApiService;
import org.apache.http.entity.StringEntity;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import sun.reflect.annotation.ExceptionProxy;

import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest(classes = TaobaoliveApplication.class, args = "test")
public class TaobaoAccountEntityTests extends BaseTests {

    @Autowired
    private TaobaoApiService taobaoApiService;

    @Autowired
    private TaobaoAccountService taobaoAccountService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    void getNewDeviceId() {
        List<TaobaoAccountEntity> taobaoAccountEntities = taobaoAccountService.list();
        for (TaobaoAccountEntity taobaoAccountEntity : taobaoAccountEntities) {
            if (taobaoAccountEntity.getState() == TaobaoAccountState.Normal.getState()) {
                R r = taobaoApiService.getH5Token(taobaoAccountEntity);
                if (r.getCode() == ErrorCodes.SUCCESS) {
                    System.out.println(taobaoAccountEntity.getNick() + ": H5Token成功");
                } else {
                    System.out.println(taobaoAccountEntity.getNick() + ": H5Token失败，" + r.getMsg());
                }

                r = taobaoApiService.getUmtidToken();
                if (r.getCode() == ErrorCodes.SUCCESS) {
                    System.out.println(taobaoAccountEntity.getNick() + ": getUmtidToken成功，" + r.get("umtid").toString());
                } else {
                    System.out.println(taobaoAccountEntity.getNick() + ": getUmtidToken失败，" + r.getMsg());
                }

                r = taobaoApiService.getNewDeviceId(taobaoAccountEntity);
                if (r.getCode() == ErrorCodes.SUCCESS) {
                    System.out.println(taobaoAccountEntity.getNick() + ": 获取机器码成功，" + r.get("device_id"));
                } else {
                    System.out.println(taobaoAccountEntity.getNick() + ": 获取机器码失败，" + r.getMsg());
                }
            }
        }
    }

    @Test
    void addAccount() {
        try {
            contextLoads();

            String url = "http://localhost:8080/tbacc/qrcode";

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setContentType("application/json")
                            .addHeader("token", accessToken),
                    new Request("POST", url, ResponseType.TEXT));

            System.out.println(response.getResult());

            ObjectMapper objectMapper = new ObjectMapper();
            R r = objectMapper.readValue(response.getResult(), R.class);

            String qrAccessToken = (String) r.get("access_token");
            String imageUrl = (String) r.get("navigate_url");

            System.out.println(imageUrl);

            for (int wait = 0; wait < 60; wait++) {
                url = "http://localhost:8080/tbacc/verify_qrcode";

                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("access_token", qrAccessToken);

                String json = objectMapper.writeValueAsString(paramMap);

                response = HttpHelper.execute(
                        new SiteConfig()
                                .setContentType("application/json")
                                .addHeader("token", accessToken),
                        new Request("POST", url, ResponseType.TEXT)
                                .setEntity(new StringEntity(json)));

                System.out.println(response.getResult());

                r = objectMapper.readValue(response.getResult(), R.class);

                if (r.getCode() == ErrorCodes.SUCCESS) {
                    break;
                }

                Thread.sleep(1000);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    void deleteAccount() {
        try {
            contextLoads();

            String nick = "简单点9791";

            List<String> accountIds = new ArrayList<>();
            accountIds.add(nick);

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("nicks", accountIds);

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(paramMap);

            String url = "http://localhost:8080/tbacc/batch_delete";

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setContentType("application/json; charset=utf-8")
                            .addHeader("token", accessToken),
                    new Request("POST", url, ResponseType.TEXT)
                            .setEntity(new StringEntity(json, "UTF-8")));

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

            String url = "http://localhost:8080/tbacc/list";

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setContentType("application/json")
                            .addHeader("token", accessToken),
                    new Request("POST", url, ResponseType.TEXT)
                            .setEntity(new StringEntity(json)));

            System.out.println(response.getResult());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
