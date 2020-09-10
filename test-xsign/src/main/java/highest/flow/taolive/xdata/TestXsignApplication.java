package highest.flow.taolive.xdata;

import com.alibaba.fastjson.JSON;
import highest.flow.taolive.xdata.http.HttpHelper;
import highest.flow.taolive.xdata.http.Request;
import highest.flow.taolive.xdata.http.ResponseType;
import highest.flow.taolive.xdata.http.SiteConfig;
import highest.flow.taolive.xdata.http.httpclient.response.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

@SpringBootApplication
public class TestXsignApplication implements ApplicationRunner {

    public static void main(String[] args) {
        SpringApplication.run(TestXsignApplication.class, args);
    }

    private String method = "md5";
    private String prefix = "gaoji";
    private String suffix = "yinliu";
    private String encryptKey = "1234!@#$";

    @Value("${threads:1000}")
    private int threadsCount;

    @Value("${count:10000}")
    private int repeatCount;

    @Value("${sign.url:http://localhost:9999/xdata}")
    private String signUrl = "";

    @Value("${sign.mode:xposed}")
    private String signMode = "";

    public void newTest(int count) {
        Map<String, Object> map = new HashMap<>();
        map.put("utdid", "");
        map.put("uid", "");
        map.put("appkey", "21646297");
        map.put("sid", "");
        map.put("ttid", "600000@taobao_android_7.6.0");
        map.put("pv", "6.2");
        map.put("devid", "");
        map.put("location1", "");
        map.put("location2", "");
        map.put("features", "27");
        map.put("subUrl", "mtop.taobao.sharepassword.querypassword");
        map.put("urlVer", "1.0");
        map.put("timestamp", 1599203806);
        map.put("data", "{\"passwordContent\":\"￥YIJNcWAbEW3￥\"}");

        long startTime = System.currentTimeMillis();

        System.out.println("TOTAL：" + count);
        int success = 0;
        for (int idx = 0; idx < count; idx++) {

            String url = "http://42.194.144.67:8080/sign?input=";

            for (String key : map.keySet()) {
                url += key + "=" + URLEncoder.encode(String.valueOf(map.get(key))) + "&";
            }

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36")
                            .addHeader("Content-Type", "application/x-www-form-urlencoded"),
                    new Request("GET", url, ResponseType.TEXT));
            if (response.getStatusCode() != HttpStatus.SC_OK) {
                break;
            }

            String respText = response.getResult();

            if (respText == null) {
                break;
            }

            System.out.println(respText);

            if (StringUtils.isNotBlank(respText)) {
                System.out.println("第" + (idx + 1) + " 成功");
                success++;
            } else {
                System.out.println("第" + (idx + 1) + " 失败");
            }
        }

        System.out.println("TOTAL: " + count + ", SUCCESS: " + success);

        long times = System.currentTimeMillis() - startTime;
        System.out.println("总共耗时：" + times + "毫秒");
    }


    public void testXData(int count) {
        Map<String, Object> map = new HashMap<>();
        map.put("utdid", "");
        map.put("uid", "");
        map.put("appkey", "25443018");
        map.put("sid", "");
        map.put("ttid", "10005533@taobaolive_android_1.4.0");
        map.put("pv", "6.2");
        map.put("devid", "");
        map.put("location1", "");
        map.put("location2", "");
        map.put("features", "27");
        map.put("subUrl", "mtop.taobao.sharepassword.querypassword");
        map.put("urlVer", "1.0");
        map.put("timestamp", 1599203806);
        map.put("data", "{\"passwordContent\":\"￥YIJNcWAbEW3￥\"}");

        long startTime = System.currentTimeMillis();

        System.out.println("TOTAL：" + count);
        int success = 0;
        for (int idx = 0; idx < count; idx++) {

            String result = callXsign(map);
            if (StringUtils.isNotBlank(result)) {
                System.out.println("第" + (idx + 1) + " 成功");
                success++;
            } else {
                System.out.println("第" + (idx + 1) + " 失败");
            }
        }

        System.out.println("TOTAL: " + count + ", SUCCESS: " + success);

        long times = System.currentTimeMillis() - startTime;
        System.out.println("总共耗时：" + times + "毫秒");
    }

    private String callXsign(Map<String, Object> map) {
        try {
            if (signMode.toLowerCase().compareTo("xposed") == 0) {
                CryptoHelper cryptoHelper = new CryptoHelper(method, prefix, suffix, encryptKey);

                String url = signUrl;

                String jsonText = JSON.toJSONString(map);
                String encoded = cryptoHelper.encrypt(jsonText);

                Map<String, String> postParams = new HashMap<>();
                postParams.put("data", encoded);
                postParams.put("sign", cryptoHelper.sign(encoded));

                Response<String> response = HttpHelper.execute(
                        new SiteConfig()
                                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36")
                                .addHeader("Content-Type", "application/x-www-form-urlencoded"),
                        new Request("POST", url, ResponseType.TEXT)
                                .setParameters(postParams));
                if (response.getStatusCode() != HttpStatus.SC_OK) {
                    return null;
                }

                String respText = response.getResult();

                if (respText == null) {
                    return null;
                }

                JsonParser jsonParser = JsonParserFactory.getJsonParser();
                Map<String, Object> mapResp = jsonParser.parseMap(respText);

                Map<String, Object> mapData = (Map) mapResp.get("data");

                String version = mapData == null || !mapData.containsKey("version") ? "" : String.valueOf(mapData.get("version"));
                String xsign = mapData == null || !mapData.containsKey("xsign") ? "" : String.valueOf(mapData.get("xsign"));
                String wua = mapData == null || !mapData.containsKey("wua") ? "" : String.valueOf(mapData.get("wua"));
                String sgext = mapData == null || !mapData.containsKey("x-sgext") ? "" : String.valueOf(mapData.get("x-sgext"));
                String miniWua = mapData == null || !mapData.containsKey("x-mini-wua") ? "" : String.valueOf(mapData.get("x-mini-wua"));
                String umt = mapData == null || !mapData.containsKey("x-umt") ? "" : String.valueOf(mapData.get("x-umt"));

                xsign = StringUtils.strip(xsign, "\"\r\n");

                if (xsign.startsWith("ab2")) {
                    return xsign;
                }

            } else if (signMode.toLowerCase().compareTo("xposed2") == 0) {
                String url = signUrl + "?";

                for (String key : map.keySet()) {
                    url += key + "=" + URLEncoder.encode(String.valueOf(map.get(key))) + "&";
                }

                Response<String> response = HttpHelper.execute(
                        new SiteConfig()
                                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36")
                                .addHeader("Content-Type", "application/x-www-form-urlencoded"),
                        new Request("GET", url, ResponseType.TEXT));
                if (response.getStatusCode() != HttpStatus.SC_OK) {
                    return null;
                }

                String respText = response.getResult();

                if (respText == null) {
                    return null;
                }

                JsonParser jsonParser = JsonParserFactory.getJsonParser();
                Map<String, Object> mapResp = jsonParser.parseMap(respText);

                Map<String, Object> mapData = (Map) mapResp.get("data");

                String version = mapData == null || !mapData.containsKey("version") ? "" : String.valueOf(mapData.get("version"));
                String xsign = mapData == null || !mapData.containsKey("xsign") ? "" : String.valueOf(mapData.get("xsign"));
                String wua = mapData == null || !mapData.containsKey("wua") ? "" : String.valueOf(mapData.get("wua"));
                String sgext = mapData == null || !mapData.containsKey("x-sgext") ? "" : String.valueOf(mapData.get("x-sgext"));
                String miniWua = mapData == null || !mapData.containsKey("x-mini-wua") ? "" : String.valueOf(mapData.get("x-mini-wua"));
                String umt = mapData == null || !mapData.containsKey("x-umt") ? "" : String.valueOf(mapData.get("x-umt"));

                xsign = StringUtils.strip(xsign, "\"\r\n");

                if (xsign.startsWith("ab2")) {
                    return xsign;
                }

            } else if (signMode.toLowerCase().compareTo("frida") == 0) {
                String url = signUrl + "?";

                for (String key : map.keySet()) {
                    url += key + "=" + URLEncoder.encode(String.valueOf(map.get(key))) + "&";
                }

                Response<String> response = HttpHelper.execute(
                        new SiteConfig()
                                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36")
                                .addHeader("Content-Type", "application/x-www-form-urlencoded"),
                        new Request("GET", url, ResponseType.TEXT));
                if (response.getStatusCode() != HttpStatus.SC_OK) {
                    return null;
                }

                String respText = response.getResult();

                if (respText == null) {
                    return null;
                }

                JsonParser jsonParser = JsonParserFactory.getJsonParser();
                Map<String, Object> mapResp = jsonParser.parseMap(respText);

                Map<String, Object> mapData = (Map) mapResp.get("data");

                String xsign = mapData == null || !mapData.containsKey("x-sign") ? "" : String.valueOf(mapData.get("x-sign"));

                xsign = StringUtils.strip(xsign, "\"\r\n");

                if (xsign.startsWith("ab2")) {
                    return xsign;
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("URL = " + signUrl);

        //newTest();

//        testXData(repeatCount);

//        testHttp(threadsCount, repeatCount);
    }

    public void testHttp(final int threadsCount, final int repeatCount) {
        try {
            ThreadPoolTaskExecutor threadPoolExecutor = new ThreadPoolTaskExecutor();
            threadPoolExecutor.setCorePoolSize(6000);
            threadPoolExecutor.setMaxPoolSize(9000);
            threadPoolExecutor.setQueueCapacity(3000);
            threadPoolExecutor.setThreadNamePrefix("ranking-");
            threadPoolExecutor.initialize();

            long startTime = System.currentTimeMillis();

            CountDownLatch countDownLatch = new CountDownLatch(threadsCount);

            for (int thread = 0; thread < threadsCount; thread++) {
                threadPoolExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
//                            for (int idx = 0; idx < repeatCount; idx++) {
//                                Response<String> response = HttpHelper.execute(
//                                        new SiteConfig()
//                                                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36")
//                                                .addHeader("Content-Type", "application/x-www-form-urlencoded"),
//                                        new Request("GET", "http://localhost:9999/xdata", ResponseType.TEXT));
//                            }

                            testXData(repeatCount);

                        } catch (Exception ex) {
                            countDownLatch.countDown();
                        }
                    }
                });
            }
            countDownLatch.await();

            long times = System.currentTimeMillis() - startTime;
            System.out.println("【线程】总共耗时：" + times + "毫秒");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
