package highest.flow.taobaolive;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class XSignTests {

    @Test
    void xsign62() {
        MTopSignParam mTopSignParam = new MTopSignParam();
        Map<String, String> p1 = new HashMap<>();

        p1.put("deviceId", "Ao4vIfj3ZiwJfs_0dduqIznLpmne4sW0xjBJi9pElAL6");
        p1.put("appKey", "21646297");
        p1.put("utdid", "XtiVIyiuolADAFtcfVW2GUAd");
        p1.put("x-features", "27");
        p1.put("ttid", "dynamicConfig");
        p1.put("v", "1.0");
        p1.put("t", "1591366145");
        p1.put("api", "mtop.client.mudp.dy.update");
        p1.put("data", "{\"identifier\":\"taobao4android\",\"configVersion\":\"0\",\"apiLevel\":\"0\",\"appVersion\":\"9.1.0\"}");
        p1.put("lng", "114.693997");
        p1.put("lat", "23.720447");


    }
}
