package highest.flow.taobaolive;

import com.fasterxml.jackson.databind.ObjectMapper;
import highest.flow.taobaolive.common.http.HttpHelper;
import highest.flow.taobaolive.common.http.Request;
import highest.flow.taobaolive.common.http.ResponseType;
import highest.flow.taobaolive.common.http.SiteConfig;
import highest.flow.taobaolive.common.http.httpclient.response.Response;
import highest.flow.taobaolive.common.utils.CommonUtils;
import highest.flow.taobaolive.common.utils.DateUtils;
import org.apache.http.entity.StringEntity;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest(classes = TaobaoliveApplication.class, args = "test")
public class SystemTests extends BaseTests {

    @Test
    void logs() {
        try {
            contextLoads();

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("page_no", 1);
            paramMap.put("page_size", 20);

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(paramMap);

            String url = host + "/v1.0/sys/logs";

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setContentType("application/json")
                            .addHeader("access_token", accessToken),
                    new Request("POST", url, ResponseType.TEXT)
                            .setEntity(new StringEntity(json)));

            System.out.println(response.getResult());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    void dashboard() {
        try {
            contextLoads();

            String url = host + "/v1.0/sys/dashboard/tbacc";

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setContentType("application/json")
                            .addHeader("access_token", accessToken),
                    new Request("POST", url, ResponseType.TEXT));

            System.out.println(response.getResult());

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("start_date", "2020-07-10");
            paramMap.put("end_date", "2020-08-10");

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(paramMap);

            url = host + "/v1.0/sys/dashboard/ranking";

            response = HttpHelper.execute(
                    new SiteConfig()
                            .setContentType("application/json")
                            .addHeader("access_token", accessToken),
                    new Request("POST", url, ResponseType.TEXT)
                            .setEntity(new StringEntity(json)));

            System.out.println(response.getResult());

            url = host + "/v1.0/sys/dashboard/live";

            response = HttpHelper.execute(
                    new SiteConfig()
                            .setContentType("application/json")
                            .addHeader("access_token", accessToken),
                    new Request("POST", url, ResponseType.TEXT));

            System.out.println(response.getResult());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    void parseDate() {
        String dateStr1 = "Mon, 23 Sep 30 11:35:21 GMT";
        Date date1 = DateUtils.parseDateStr(dateStr1);
        if (date1 == null) {
            System.out.println("parse as null");
        } else {
            System.out.println(CommonUtils.formatDate(date1));
        }

        String dateStr2 = "Fri, 25 Sep 2020 09:41:42 GMT";
        Date date2 = DateUtils.parseDateStr(dateStr2);
        if (date2 == null) {
            System.out.println("parse as null");
        } else {
            System.out.println(CommonUtils.formatDate(date2));
        }

        String dateStr3 = "2021-09-25 16:50:49";
        Date date3 = DateUtils.parseDateStr(dateStr3);
        if (date3 == null) {
            System.out.println("parse as null");
        } else {
            System.out.println(CommonUtils.formatDate(date3));
        }
    }

}
