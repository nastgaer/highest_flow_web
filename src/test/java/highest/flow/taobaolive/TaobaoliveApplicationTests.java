package highest.flow.taobaolive;

import com.fasterxml.jackson.databind.ObjectMapper;
import highest.flow.taobaolive.common.http.HttpHelper;
import highest.flow.taobaolive.common.http.Request;
import highest.flow.taobaolive.common.http.ResponseType;
import highest.flow.taobaolive.common.http.response.Response;
import highest.flow.taobaolive.common.utils.HttpUtils;
import highest.flow.taobaolive.taobao.entity.XHeader;
import highest.flow.taobaolive.taobao.service.XSignService;
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

    @Autowired
    private XSignService xSignService;

    @Test
    void contextLoads() {
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
            xHeader.setData("{}");

            ObjectMapper objectMapper = new ObjectMapper();
            String data = objectMapper.writeValueAsString(xHeader);

            System.out.println("plain=" + data);

            HashMap<String, String> paramMap = new HashMap<>();
            paramMap.put("data", data);
            paramMap.put("sign", "");

            System.out.println(data);

//            Request request = new Request("POST", "http://119.45.148.200:8080/xsign", ResponseType.TEXT);
//            request.addParameters(paramMap);

//            HttpHelper httpHelper = new HttpHelper();
//            Response<String> response = httpHelper.execute(request);

//            System.out.println(response.getResult());

            String xsign = HttpUtils.sendPost("http://localhost:8088/xsign", paramMap);
            System.out.println("sign=" + xsign);

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
