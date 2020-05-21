package highest.flow.taobaolive;

import com.fasterxml.jackson.databind.ObjectMapper;
import highest.flow.taobaolive.common.http.HttpHelper;
import highest.flow.taobaolive.common.http.Request;
import highest.flow.taobaolive.common.http.ResponseType;
import highest.flow.taobaolive.common.http.response.Response;
import highest.flow.taobaolive.taobao.entity.XHeader;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@SpringBootTest
class TaobaoliveApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void getXSign() {
        try {
            XHeader xHeader = new XHeader(new Date());

            xHeader.setUtdid("utdid");
            xHeader.setUid("uid");
            xHeader.setAppkey("appkey");
            xHeader.setAes("aes");
            xHeader.setTimestamp(new Date());
            xHeader.setUrl("url");
            xHeader.setUrlVer("urlVer");
            xHeader.setSid("sid");
            xHeader.setTtid("ttid");
            xHeader.setDevid("devid");
            xHeader.setLocation("location");
            xHeader.setFeatures("features");

            ObjectMapper objectMapper = new ObjectMapper();
            String data = objectMapper.writeValueAsString(xHeader);

            System.out.println("plain=" + data);

            HashMap<String, String> paramMap = new HashMap<>();
            paramMap.put("data", data);
            paramMap.put("sign", "");

            Request request = new Request("POST", "http://localhost:8088/xsign", ResponseType.TEXT);
            request.addParameters(paramMap);

            //String xsign = HttpUtils.sendPost("http://localhost:8088/xsign", paramMap);
            //System.out.println("sign=" + xsign);

            HttpHelper httpHelper = new HttpHelper();
            Response<String> response = httpHelper.execute(request);

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

            Request request = new Request("GET", "https://stackoverflow.com");
            request.setCookieStore(newCookieStore);

            HttpHelper httpHelper = new HttpHelper();
            Response<String> response = httpHelper.execute(request);

            List<Cookie> cookies =  response.getCookieStore().getCookies();
            for (Cookie cookie : cookies) {
                System.out.println(cookie.toString());
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Autowired
    DataSource dataSource;

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

}
