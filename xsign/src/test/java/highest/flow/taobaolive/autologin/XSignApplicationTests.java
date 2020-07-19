package highest.flow.taobaolive.autologin;

import highest.flow.taobaolive.common.http.httpclient.response.Response;
import highest.flow.taobaolive.common.utils.CryptoUtils;
import highest.flow.taobaolive.service.CryptoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
class XSignApplicationTests {

    @Autowired
    private CryptoService cryptoService;

    @Value("${sign.prefix}")
    private String prefix;
    @Value("${sign.suffix}")
    private String suffix;
    @Value("${sign.method}")
    private String method;

    @Test
    void testXSign() {
        try {


            String encryptData = cryptoService.encrypt(data);
            String xsign = CryptoUtils.MD5(prefix + encryptData + suffix);

            Map<String, String> paramMap1 = new HashMap<>();
            paramMap1.put("data", encryptData);
            paramMap1.put("xsign", xsign);

            String url = "http://localhost:8080/v1.0/license/generate";
            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setContentType("application/x-www-form-urlencoded"),
                    new Request("POST", url, ResponseType.TEXT)
                            .addParameter("token", token)
                            .addParameters(paramMap1));

            System.out.println(response.getResult());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
