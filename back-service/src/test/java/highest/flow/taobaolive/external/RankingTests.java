package highest.flow.taobaolive.external;

import com.fasterxml.jackson.databind.ObjectMapper;
import highest.flow.taobaolive.BaseTests;
import highest.flow.taobaolive.TaobaoliveApplication;
import highest.flow.taobaolive.common.http.HttpHelper;
import highest.flow.taobaolive.common.http.Request;
import highest.flow.taobaolive.common.http.ResponseType;
import highest.flow.taobaolive.common.http.SiteConfig;
import highest.flow.taobaolive.common.http.httpclient.response.Response;
import highest.flow.taobaolive.common.utils.CommonUtils;
import highest.flow.taobaolive.common.utils.CryptoUtils;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.security.defines.LicenseCodeState;
import highest.flow.taobaolive.security.defines.LicenseCodeType;
import highest.flow.taobaolive.security.entity.LicenseCode;
import highest.flow.taobaolive.security.service.CryptoService;
import highest.flow.taobaolive.security.service.LicenseCodeService;
import highest.flow.taobaolive.sys.defines.MemberServiceType;
import org.apache.http.entity.StringEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest(classes = TaobaoliveApplication.class, args = "test")
public class RankingTests {

    @Value("${sign.prefix}")
    private String prefix;
    @Value("${sign.suffix}")
    private String suffix;
    @Value("${sign.method}")
    private String method;

    @Autowired
    private CryptoService cryptoService;

    @Autowired
    private LicenseCodeService licenseCodeService;

    private String accessToken;

    private static String code = "testeX5e53Df";
    private static String machineCode = "machinetesteX5e53Df";

    @Test
    void addInternalCode() {
        try {
            int count = 10;
            for (int idx = 0; idx < count; idx++) {
                String code1 = "internal" + CommonUtils.randomAlphabetic(8);

                LicenseCode licenseCode = new LicenseCode();
                licenseCode.setCodeType(LicenseCodeType.Internal.getType());
                licenseCode.setServiceType(MemberServiceType.Ranking.getServiceType());
                licenseCode.setHours(24 * 30);
                licenseCode.setCode(code1);
                licenseCode.setState(LicenseCodeState.Created.getState());
                licenseCode.setCreatedTime(new Date());

                licenseCodeService.save(licenseCode);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    void contextLoads() {
        try {
            Map<String, Object> baseParam = new HashMap<>();
            baseParam.put("code", code);
            baseParam.put("machineCode", machineCode);

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(baseParam);

            String encryptData = cryptoService.encrypt(json);
            String sign = CryptoUtils.MD5(prefix + encryptData + suffix);

            HashMap<String, String> paramMap = new HashMap<>();
            paramMap.put("version", "v1.1");
            paramMap.put("api", "register_code");
            paramMap.put("data", encryptData);
            paramMap.put("sign", sign);

            json = objectMapper.writeValueAsString(paramMap);

            String url = "http://localhost:8080/external/ranking/reg";

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setContentType("application/json"),
                    new Request("POST", url, ResponseType.TEXT)
                            .setEntity(new StringEntity(json, "UTF-8")));

            System.out.println(response.getResult());

            R r = objectMapper.readValue(response.getResult(), R.class);

            System.out.println(r.get("access_token"));

            this.accessToken = String.valueOf(r.get("access_token"));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    void tbacc() {
        try {
            contextLoads();

            Map<String, Object> baseParam = new HashMap<>();
            baseParam.put("pageNo", 1);
            baseParam.put("pageSize", 20);

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(baseParam);

            String encryptData = cryptoService.encrypt(json);
            String sign = CryptoUtils.MD5(prefix + encryptData + suffix);

            HashMap<String, String> paramMap = new HashMap<>();
            paramMap.put("version", "v1.1");
            paramMap.put("api", "tbacc");
            paramMap.put("data", encryptData);
            paramMap.put("sign", sign);

            json = objectMapper.writeValueAsString(paramMap);

            String url = "http://localhost:8080/external/ranking/api";

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
    void parseTaocode() {
        try {
            contextLoads();

            Map<String, Object> baseParam = new HashMap<>();
            baseParam.put("taocode", "￥5o3m1zAc4Tp￥");

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(baseParam);

            String encryptData = cryptoService.encrypt(json);
            String sign = CryptoUtils.MD5(prefix + encryptData + suffix);

            HashMap<String, String> paramMap = new HashMap<>();
            paramMap.put("version", "v1.1");
            paramMap.put("api", "parse_taocode");
            paramMap.put("data", encryptData);
            paramMap.put("sign", sign);

            json = objectMapper.writeValueAsString(paramMap);

            String url = "http://localhost:8080/external/ranking/api";

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
    void addTask() {
        try {
            contextLoads();

            Map<String, Object> baseParam = new HashMap<>();
            baseParam.put("taocode", "￥5o3m1zAc4Tp￥");
            baseParam.put("targetScore", 20000);
            baseParam.put("doubleBuy", false);
            baseParam.put("startTime", null);

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(baseParam);

            String encryptData = cryptoService.encrypt(json);
            String sign = CryptoUtils.MD5(prefix + encryptData + suffix);

            HashMap<String, String> paramMap = new HashMap<>();
            paramMap.put("version", "v1.1");
            paramMap.put("api", "start_task");
            paramMap.put("data", encryptData);
            paramMap.put("sign", sign);

            json = objectMapper.writeValueAsString(paramMap);

            String url = "http://localhost:8080/external/ranking/api";

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
    void getStatus() {
        try {
            contextLoads();

            Map<String, Object> baseParam = new HashMap<>();
            baseParam.put("task_id", 13);

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(baseParam);

            String encryptData = cryptoService.encrypt(json);
            String sign = CryptoUtils.MD5(prefix + encryptData + suffix);

            HashMap<String, String> paramMap = new HashMap<>();
            paramMap.put("version", "v1.1");
            paramMap.put("api", "get_status");
            paramMap.put("data", encryptData);
            paramMap.put("sign", sign);

            json = objectMapper.writeValueAsString(paramMap);

            String url = "http://localhost:8080/external/ranking/api";

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
    void stopTask() {
        try {
            contextLoads();

            Map<String, Object> baseParam = new HashMap<>();
            baseParam.put("task_id", 13);

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(baseParam);

            String encryptData = cryptoService.encrypt(json);
            String sign = CryptoUtils.MD5(prefix + encryptData + suffix);

            HashMap<String, String> paramMap = new HashMap<>();
            paramMap.put("version", "v1.1");
            paramMap.put("api", "stop_task");
            paramMap.put("data", encryptData);
            paramMap.put("sign", sign);

            json = objectMapper.writeValueAsString(paramMap);

            String url = "http://localhost:8080/external/ranking/api";

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
