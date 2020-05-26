package highest.flow.taobaolive;

import com.fasterxml.jackson.databind.ObjectMapper;
import highest.flow.taobaolive.app.defines.HFUserLevel;
import highest.flow.taobaolive.common.defines.ServiceType;
import highest.flow.taobaolive.common.http.HttpHelper;
import highest.flow.taobaolive.common.http.Request;
import highest.flow.taobaolive.common.http.ResponseType;
import highest.flow.taobaolive.common.http.response.Response;
import highest.flow.taobaolive.common.utils.CryptoUtils;
import highest.flow.taobaolive.security.service.CryptoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.boot.test.context.SpringBootTest;

import javax.xml.ws.Service;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class LicenseTests {

    @Autowired
    private CryptoService cryptoService;

    @Value("${sign.prefix}")
    private String prefix;
    @Value("${sign.suffix}")
    private String suffix;
    @Value("${sign.method}")
    private String method;

    @Test
    void registerAdministrator() {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("username", "administrator");
            paramMap.put("password", "password");
            paramMap.put("machine_code", "machine_code");
            paramMap.put("mobile", "mobile");
            paramMap.put("weixin", "weixin");
            paramMap.put("level", HFUserLevel.Adminitrator.getLevel());
            paramMap.put("service_type", ServiceType.高级引流.getServiceType());

            ObjectMapper objectMapper = new ObjectMapper();
            String plain = objectMapper.writeValueAsString(paramMap);

            System.out.println("plain=" + plain);

            String encryptedData = cryptoService.encrypt(plain);
            String sign = CryptoUtils.MD5(prefix + encryptedData + suffix);

            Map<String, String> reqParamMap = new HashMap<>();
            reqParamMap.put("data", encryptedData);
            reqParamMap.put("sign", sign);

            String url = "http://localhost:8080/sys/register";
            Request request = new Request("POST", url, ResponseType.TEXT);
            request.addParameters(reqParamMap);

            HttpHelper httpHelper = new HttpHelper();
            Response<String> response = httpHelper.execute(request);

            System.out.println(response.getResult());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String getToken() {
        try {
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("username", "administrator");
            paramMap.put("password", "password");
            paramMap.put("machine_code", "machine_code");

            ObjectMapper objectMapper = new ObjectMapper();
            String plain = objectMapper.writeValueAsString(paramMap);

            System.out.println("plain=" + plain);

            String encryptedData = cryptoService.encrypt(plain);
            String sign = CryptoUtils.MD5(prefix + encryptedData + suffix);

            paramMap.clear();
            paramMap.put("data", encryptedData);
            paramMap.put("sign", sign);

            String url = "http://localhost:8080/sys/login";
            Request request = new Request("POST", url, ResponseType.TEXT);
            request.addParameters(paramMap);

            HttpHelper httpHelper = new HttpHelper();
            Response<String> response = httpHelper.execute(request);

            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> resultMap = jsonParser.parseMap(response.getResult());

            String token = (String)resultMap.get("token");
            return token;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Test
    void generateCode() {
        try {
            String token = getToken();

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("hours", 24);
            paramMap.put("service_type", ServiceType.高级引流.getServiceType());

            ObjectMapper objectMapper = new ObjectMapper();
            String data = objectMapper.writeValueAsString(paramMap);

            System.out.println("plain=" + data);

            String encryptData = cryptoService.encrypt(data);
            String sign = CryptoUtils.MD5(prefix + encryptData + suffix);

            Map<String, String> paramMap1 = new HashMap<>();
            paramMap1.put("data", encryptData);
            paramMap1.put("sign", sign);

            String url = "http://localhost:8080/license/generate";
            Request request = new Request("POST", url, ResponseType.TEXT);
            request.addParameters(paramMap1);
            request.addHeader("token", token);

            HttpHelper httpHelper = new HttpHelper();
            Response<String> response = httpHelper.execute(request);

            System.out.println(response.getResult());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
