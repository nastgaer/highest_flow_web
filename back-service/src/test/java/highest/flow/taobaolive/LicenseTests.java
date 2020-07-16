package highest.flow.taobaolive;

import com.fasterxml.jackson.databind.ObjectMapper;
import highest.flow.taobaolive.common.defines.ServiceType;
import highest.flow.taobaolive.common.http.HttpHelper;
import highest.flow.taobaolive.common.http.SiteConfig;
import highest.flow.taobaolive.common.http.Request;
import highest.flow.taobaolive.common.http.ResponseType;
import highest.flow.taobaolive.common.http.httpclient.response.Response;
import highest.flow.taobaolive.common.utils.CryptoUtils;
import highest.flow.taobaolive.security.service.CryptoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class LicenseTests {

//    @Autowired
//    private CryptoService cryptoService;
//
//    @Value("${sign.prefix}")
//    private String prefix;
//    @Value("${sign.suffix}")
//    private String suffix;
//    @Value("${sign.method}")
//    private String method;
//
//    @Test
//    void registerAdministrator() {
//        try {
//            Map<String, Object> paramMap = new HashMap<>();
//            paramMap.put("username", "administrator");
//            paramMap.put("password", "password");
//            paramMap.put("mobile", "mobile");
//            paramMap.put("weixin", "weixin");
//            paramMap.put("level", UserLevel.Adminitrator.getLevel());
//            paramMap.put("service_type", ServiceType.高级引流.getServiceType());
//
//            ObjectMapper objectMapper = new ObjectMapper();
//            String plain = objectMapper.writeValueAsString(paramMap);
//
//            System.out.println("plain=" + plain);
//
//            String encryptedData = cryptoService.encrypt(plain);
//            String xsign = CryptoUtils.MD5(prefix + encryptedData + suffix);
//
//            Map<String, String> reqParamMap = new HashMap<>();
//            reqParamMap.put("data", encryptedData);
//            reqParamMap.put("xsign", xsign);
//
//            String url = "http://localhost:8080/v1.0/sys/register";
//
//            Response<String> response = HttpHelper.execute(
//                    new SiteConfig()
//                            .setContentType("application/x-www-form-urlencoded"),
//                    new Request("POST", url, ResponseType.TEXT)
//                            .addParameters(reqParamMap));
//
//            System.out.println(response.getResult());
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }
//
//    private String getAdministratorToken() {
//        try {
//            Map<String, String> paramMap = new HashMap<>();
//            paramMap.put("username", "administrator");
//            paramMap.put("password", "password");
//            paramMap.put("machine_code", "machine_code");
//
//            ObjectMapper objectMapper = new ObjectMapper();
//            String plain = objectMapper.writeValueAsString(paramMap);
//
//            System.out.println("plain=" + plain);
//
//            String encryptedData = cryptoService.encrypt(plain);
//            String xsign = CryptoUtils.MD5(prefix + encryptedData + suffix);
//
//            paramMap.clear();
//            paramMap.put("data", encryptedData);
//            paramMap.put("xsign", xsign);
//
//            String url = "http://localhost:8080/v1.0/sys/login";
//            Response<String> response = HttpHelper.execute(
//                    new SiteConfig()
//                            .setContentType("application/x-www-form-urlencoded"),
//                    new Request("POST", url, ResponseType.TEXT)
//                            .addParameters(paramMap));
//
//            JsonParser jsonParser = JsonParserFactory.getJsonParser();
//            Map<String, Object> resultMap = jsonParser.parseMap(response.getResult());
//
//            String token = (String) resultMap.get("token");
//            return token;
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return null;
//    }
//
//    @Test
//    void generateCode() {
//        try {
//            String token = getAdministratorToken();
//
//            Map<String, Object> paramMap = new HashMap<>();
//            paramMap.put("hours", 24);
//            paramMap.put("service_type", ServiceType.高级引流.getServiceType());
//
//            ObjectMapper objectMapper = new ObjectMapper();
//            String data = objectMapper.writeValueAsString(paramMap);
//
//            System.out.println("plain=" + data);
//
//            String encryptData = cryptoService.encrypt(data);
//            String xsign = CryptoUtils.MD5(prefix + encryptData + suffix);
//
//            Map<String, String> paramMap1 = new HashMap<>();
//            paramMap1.put("data", encryptData);
//            paramMap1.put("xsign", xsign);
//
//            String url = "http://localhost:8080/v1.0/license/generate";
//            Response<String> response = HttpHelper.execute(
//                    new SiteConfig()
//                            .setContentType("application/x-www-form-urlencoded"),
//                    new Request("POST", url, ResponseType.TEXT)
//                            .addParameter("token", token)
//                            .addParameters(paramMap1));
//
//            System.out.println(response.getResult());
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }
//
//    @Test
//    void acceptCode() {
//        try {
//            Map<String, Object> paramMap = new HashMap<>();
//            paramMap.put("code", "BA2A867CC27907C53946FD5EA47DAFB90EFC672E624B2B20C7B4D167FD40003E");
//            paramMap.put("machine_code", "machine_code");
//
//            ObjectMapper objectMapper = new ObjectMapper();
//            String data = objectMapper.writeValueAsString(paramMap);
//
//            System.out.println("plain=" + data);
//
//            String encryptData = cryptoService.encrypt(data);
//            String xsign = CryptoUtils.MD5(prefix + encryptData + suffix);
//
//            Map<String, String> paramMap1 = new HashMap<>();
//            paramMap1.put("data", encryptData);
//            paramMap1.put("xsign", xsign);
//
//            String url = "http://localhost:8080/v1.0/license/accept";
//
//            Response<String> response = HttpHelper.execute(
//                    new SiteConfig()
//                            .setContentType("application/x-www-form-urlencoded"),
//                    new Request("POST", url, ResponseType.TEXT)
//                            .addParameters(paramMap1));
//
//            System.out.println(response.getResult());
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }
//
//    @Test
//    void bindCode() {
//        try {
//            Map<String, Object> paramMap = new HashMap<>();
//            paramMap.put("code", "6D504C39BBA68624C3000AABB2186A5D0EFC672E624B2B20C7B4D167FD40003E");
//            paramMap.put("username", "username");
//            paramMap.put("account_id", "account_id");
//
//            ObjectMapper objectMapper = new ObjectMapper();
//            String data = objectMapper.writeValueAsString(paramMap);
//
//            System.out.println("plain=" + data);
//
//            String encryptData = cryptoService.encrypt(data);
//            String xsign = CryptoUtils.MD5(prefix + encryptData + suffix);
//
//            Map<String, String> paramMap1 = new HashMap<>();
//            paramMap1.put("data", encryptData);
//            paramMap1.put("xsign", xsign);
//
//            String url = "http://localhost:8080/v1.0/license/bind";
//
//            Response<String> response = HttpHelper.execute(
//                    new SiteConfig()
//                            .setContentType("application/x-www-form-urlencoded"),
//                    new Request("POST", url, ResponseType.TEXT)
//                            .addParameters(paramMap1));
//
//            System.out.println(response.getResult());
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }
}
