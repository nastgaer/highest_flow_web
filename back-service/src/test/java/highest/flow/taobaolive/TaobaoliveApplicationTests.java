package highest.flow.taobaolive;

import com.fasterxml.jackson.databind.ObjectMapper;
import highest.flow.taobaolive.common.http.HttpHelper;
import highest.flow.taobaolive.common.http.SiteConfig;
import highest.flow.taobaolive.common.http.cookie.DefaultCookieStorePool;
import highest.flow.taobaolive.common.http.httpclient.HttpClientFactory;
import highest.flow.taobaolive.common.http.Request;
import highest.flow.taobaolive.common.http.ResponseType;
import highest.flow.taobaolive.common.http.httpclient.response.Response;
import highest.flow.taobaolive.common.utils.CryptoUtils;
import highest.flow.taobaolive.common.utils.HFStringUtils;
import highest.flow.taobaolive.security.service.CryptoService;
import highest.flow.taobaolive.taobao.entity.XHeader;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@SpringBootTest
class TaobaoliveApplicationTests {

    @Autowired
    DataSource dataSource;

    @Autowired
    private CryptoService cryptoService;

    @Value("${xsign.prefix}")
    private String prefix;
    @Value("${xsign.suffix}")
    private String suffix;
    @Value("${xsign.method}")
    private String method;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    void contextLoads() {
        logger.trace("trace logging");
        logger.info("info logging");
    }

    @Test
    void getXSign() {
        try {
            XHeader xHeader = new XHeader(new Date());

            xHeader.setUtdid("utdid");
            xHeader.setUid("uid");
            xHeader.setAppkey("21646297");
            xHeader.setSid("sid");
            xHeader.setTtid("600000@taobao_android_7.6.0");
            xHeader.setPv("5.1");
            xHeader.setDevid("devid");
            xHeader.setLocation1("1568.459875");
            xHeader.setLocation2("454.451236");
            xHeader.setFeatures("27");
            xHeader.setSubUrl("mtop.user.getUserSimple");
            xHeader.setUrlVer("1.0");
            xHeader.setTimestamp(xHeader.getShortTimestamp());
            xHeader.setData("{}");

            ObjectMapper objectMapper = new ObjectMapper();
            String data = objectMapper.writeValueAsString(xHeader);

            System.out.println("plain=" + data);

            String encryptData = cryptoService.encrypt(data);
            String sign = CryptoUtils.MD5(prefix + encryptData + suffix);

            HashMap<String, String> paramMap = new HashMap<>();
            paramMap.put("data", encryptData);
            paramMap.put("xsign", sign);

            System.out.println(data);

            // String url = "http://119.45.148.200:8080/highest/xsign";
            String url = "http://localhost:8080/xsign";
            // String url = "http://localhost:8080/taobaolive-0.0.1-SNAPSHOT/xsign";

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setContentType("application/x-www-form-urlencoded"),
                    new Request("POST", url, ResponseType.TEXT)
                            .addParameters(paramMap));

            System.out.println(response.getResult());

        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }

    @Test
    void testCookies() {
        try {
            CookieStore newCookieStore = new BasicCookieStore();
            newCookieStore.addCookie(new BasicClientCookie("test", "value"));

            Response<String> response = HttpHelper.execute(
                    new SiteConfig(),
                    new Request("GET", "https://stackoverflow.com", ResponseType.TEXT),
                    new DefaultCookieStorePool(newCookieStore));

            List<Cookie> cookies = response.getCookieStore().getCookies();
            for (Cookie cookie : cookies) {
                System.out.println(cookie.toString());
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void testMySQL() {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            System.out.println(connection);
            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void encryptDecrypt() {
        try {
            String encrypted = cryptoService.encrypt("高级引流");
            System.out.println(encrypted);

            String decrypted = cryptoService.decrypt(encrypted);
            System.out.println(decrypted);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void byteHex() {
        try {
            String text = "高级引流";
            byte[] bytes = text.getBytes();

            String hex = HFStringUtils.byteArrayToHexString(bytes);
            System.out.println(hex);

            bytes = HFStringUtils.hexStringToByteArray(hex);
            System.out.println(new String(bytes));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
