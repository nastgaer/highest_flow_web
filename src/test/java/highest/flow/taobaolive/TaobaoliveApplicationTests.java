package highest.flow.taobaolive;

import com.fasterxml.jackson.databind.ObjectMapper;
import highest.flow.taobaolive.common.utils.HttpUtils;
import highest.flow.taobaolive.security.entity.XHeader;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

@SpringBootTest
class TaobaoliveApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void getXSign() {
        try {
            XHeader xHeader = new XHeader();

            xHeader.setUtdid("utdid");
            xHeader.setUid("uid");
            xHeader.setAppkey("appkey");
            xHeader.setAes("aes");
            xHeader.setTimestamp("timestamp");
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

            String xsign = HttpUtils.sendPost("http://localhost:8088/xsign", paramMap);
            System.out.println("sign=" + xsign);

        } catch (Exception ex) {
            System.out.println(ex.toString());
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
