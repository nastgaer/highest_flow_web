package highest.flow.taolive.xdata;

import com.alibaba.fastjson.JSON;
import highest.flow.taolive.xdata.http.HttpHelper;
import highest.flow.taolive.xdata.http.Request;
import highest.flow.taolive.xdata.http.ResponseType;
import highest.flow.taolive.xdata.http.SiteConfig;
import highest.flow.taolive.xdata.http.cookie.DefaultCookieStorePool;
import highest.flow.taolive.xdata.http.httpclient.response.Response;
import okhttp3.*;
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

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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

    @Value("${http.mode:httpclient}")
    private String httpMode = "";

    @Value("${http.async:false}")
    private boolean async = false;

    private static OkHttpClient client = null;

    private AtomicInteger success = new AtomicInteger(0), fail = new AtomicInteger(0);

    public static long dateToTimestamp(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.getTimeInMillis();
    }

    public void newTest(int count) {
        Map<String, Object> map = new HashMap<>();
        map.put("utdid", "GJ6cBpkAxoCOp5s2y3qKSD8L");
        map.put("uid", "2207677028190");
        map.put("appkey", "21646297");
        map.put("sid", "1dd69c8d1b25c9045e1c20bc3e3a2684");
        map.put("ttid", "10005533@taobaolive_android_1.8.4");
        map.put("pv", "6.3");
        map.put("devid", "ApYZCzBkSHS18kS83Zxs6Thd0Z3--aPA_WymhqHYLkE7");
        map.put("location1", "1568.459875");
        map.put("location2", "454.451236");
        map.put("features", "27");
        map.put("subUrl", "mtop.mediaplatform.hierarchy.detail");
        map.put("urlVer", "1.0");
        map.put("timestamp", 1599203806);
        map.put("data", "{\"accountId\":\"13558937\",\"liveId\":\"283552319336\"}");

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
                            .setMaxConnTotal(6000)
                            .setMaxConnPerRoute(1000)
                            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36")
                            .addHeader("Content-Type", "application/x-www-form-urlencoded"),
                    new Request("GET", url, ResponseType.TEXT));
            if (response.getStatusCode() != HttpStatus.SC_OK) {
                System.out.println("ERROR OCCURRED: " + response.getStatusCode());
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

    abstract class XSignCallback {

        abstract void onFailure();

        abstract void onSuccess(String respText);

        private int index = 0;

        public XSignCallback(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }

    private void asyncCallXsign(Map<String, Object> map, XSignCallback callback) {
        if (httpMode.compareToIgnoreCase("httpclient") == 0) {
            return;
        }

        CryptoHelper cryptoHelper = new CryptoHelper(method, prefix, suffix, encryptKey);

        String url = signUrl;

        String jsonText = JSON.toJSONString(map);
        String encoded = cryptoHelper.encrypt(jsonText);

        RequestBody requestBody = new FormBody.Builder()
                .add("data", encoded)
                .add("sign", cryptoHelper.sign(encoded))
                .build();

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        if (client == null) {
            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .connectTimeout(45000, TimeUnit.SECONDS)
                    .readTimeout(45000, TimeUnit.SECONDS);
            client = builder.build();
        }

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure();
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                String respText = response.body().string();
                callback.onSuccess(respText);
            }
        });
    }

    private Map<String, String> callXsign(Map<String, Object> map) {
        try {
//            System.out.println(JSON.toJSONString(map));

            if (signMode.toLowerCase().compareTo("xposed") == 0) {
                CryptoHelper cryptoHelper = new CryptoHelper(method, prefix, suffix, encryptKey);

                String url = signUrl;

                String jsonText = JSON.toJSONString(map);
                String encoded = cryptoHelper.encrypt(jsonText);

                String respText = null;

                if (httpMode.compareToIgnoreCase("httpclient") == 0) {
                    Map<String, String> postParams = new HashMap<>();
                    postParams.put("data", encoded);
                    postParams.put("sign", cryptoHelper.sign(encoded));

                    Response<String> response = HttpHelper.execute(
                            new SiteConfig()
                                    .setMaxConnTotal(6000)
                                    .setMaxConnPerRoute(1000)
                                    .setUserAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36")
                                    .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8"),
                            new Request("POST", url, ResponseType.TEXT)
                                    .setParameters(postParams));
                    if (response.getStatusCode() != HttpStatus.SC_OK) {
                        System.out.println("ERROR OCCURRED: " + response.getStatusCode());
                        return null;
                    }

                    respText = response.getResult();

                } else { // use okhttp
                    RequestBody requestBody = new FormBody.Builder()
                            .add("data", encoded)
                            .add("sign", cryptoHelper.sign(encoded))
                            .build();

                    okhttp3.Request request = new okhttp3.Request.Builder()
                            .url(url)
                            .post(requestBody)
                            .build();

                    if (client == null) {
                        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                                .connectTimeout(45000, TimeUnit.SECONDS)
                                .readTimeout(45000, TimeUnit.SECONDS);
                        client = builder.build();
                    }

                    Call call = client.newCall(request);
                    okhttp3.Response response = call.execute();

                    respText = response.body().string();

//                    response.body().close();
                }

                if (respText == null) {
                    System.out.println("NULL RESPONSE");
                    return null;
                }

//                System.out.println(respText);

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

                if (StringUtils.isNotEmpty(xsign) && StringUtils.isNotEmpty(miniWua)) {
                    Map<String, String> signs = new HashMap<>();
                    signs.put("x-sign", xsign);
                    signs.put("x-sgext", sgext);
                    signs.put("x-mini-wua", miniWua);
                    signs.put("x-umt", umt);
                    return signs;
                }

            } else if (signMode.toLowerCase().compareTo("xposed2") == 0 || signMode.toLowerCase().compareTo("frida") == 0) {
                String url = signUrl + "?";

                for (String key : map.keySet()) {
                    url += key + "=" + URLEncoder.encode(String.valueOf(map.get(key))) + "&";
                }

                String respText = null;

                if (httpMode.compareToIgnoreCase("httpclient") == 0) {
                    Response<String> response = HttpHelper.execute(
                            new SiteConfig()
                                    .setMaxConnTotal(6000)
                                    .setMaxConnPerRoute(1000)
                                    .setUserAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36")
                                    .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8"),
                            new Request("GET", url, ResponseType.TEXT));
                    if (response.getStatusCode() != HttpStatus.SC_OK) {
                        System.out.println("ERROR OCCURRED: " + response.getStatusCode());
                        return null;
                    }

                    respText = response.getResult();

                } else { // use okhttp
                    okhttp3.Request request = new okhttp3.Request.Builder()
                            .url(url)
                            .build();

                    if (client == null) {
                        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                                .connectTimeout(45000, TimeUnit.SECONDS)
                                .readTimeout(45000, TimeUnit.SECONDS);
                        client = builder.build();
                    }

                    Call call = client.newCall(request);
                    okhttp3.Response response = call.execute();

                    respText = response.body().string();

//                    response.body().close();
                }

                if (respText == null) {
                    System.out.println("NULL RESPONSE");
                    return null;
                }

//                System.out.println(respText);

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

                if (StringUtils.isNotEmpty(xsign) && StringUtils.isNotEmpty(miniWua)) {

                    Map<String, String> signs = new HashMap<>();
                    signs.put("x-sign", xsign);
                    signs.put("x-sgext", sgext);
                    signs.put("x-mini-wua", miniWua);
                    signs.put("x-umt", umt);
                    return signs;
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

        testHttp(threadsCount, repeatCount);
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

            CountDownLatch countDownLatch = new CountDownLatch(threadsCount * repeatCount);

            for (int thread = 0; thread < threadsCount; thread++) {
                threadPoolExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            long timestamp = 1603217301167L;
                            Map<String, Object> map = new HashMap<>();
                            map.put("utdid", "GJ6cBpkAxoCOp5s2y3qKSD8L");
                            map.put("uid", "2207677028190");
                            map.put("appkey", "25443018");
                            map.put("sid", "1dd69c8d1b25c9045e1c20bc3e3a2684");
                            map.put("ttid", "10005533@taobaolive_android_1.8.4");
                            map.put("pv", "6.3");
                            map.put("devid", "ApYZCzBkSHS18kS83Zxs6Thd0Z3--aPA_WymhqHYLkE7");
                            map.put("location1", "1568.459875");
                            map.put("location2", "454.451236");
                            map.put("features", "27");
                            map.put("subUrl", "mtop.mediaplatform.hierarchy.detail");
                            map.put("urlVer", "1.0");
                            map.put("timestamp", timestamp);
                            map.put("data", "{\"accountId\":\"13558937\",\"liveId\":\"283552319336\"}");

                            if (async) {
                                for (int idx = 0; idx < repeatCount; idx++) {
                                    XSignCallback callback = new XSignCallback(idx) {

                                        @Override
                                        public void onFailure() {
                                            System.out.println("第" + (this.getIndex() + 1) + " 失败");
                                            fail.addAndGet(1);
                                            countDownLatch.countDown();
                                        }

                                        @Override
                                        public void onSuccess(String respText) {
                                            if (StringUtils.isEmpty(respText)) {
                                                onFailure();
                                                return;
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

                                            if (StringUtils.isNotEmpty(xsign) && StringUtils.isNotEmpty(miniWua)) {
                                                System.out.println("第" + (this.getIndex() + 1) + " 成功");
                                                success.incrementAndGet();
                                            } else {
                                                System.out.println("第" + (this.getIndex() + 1) + " 失败");
                                                fail.addAndGet(1);
                                            }
                                            countDownLatch.countDown();
                                        }
                                    };

                                    asyncCallXsign(map, callback);
                                }

                            } else {
                                for (int idx = 0; idx < repeatCount; idx++) {
                                    Map<String, String> signs = callXsign(map);
                                    if (signs != null) {

                                        map.put("utdid", "GJ6cBpkAxoCOp5s2y3qKSD8L");
                                        map.put("uid", "2207677028190");
                                        map.put("appkey", "25443018");
                                        map.put("sid", "1dd69c8d1b25c9045e1c20bc3e3a2684");
                                        map.put("ttid", "10005533@taobaolive_android_1.8.4");
                                        map.put("pv", "6.3");
                                        map.put("devid", "ApYZCzBkSHS18kS83Zxs6Thd0Z3--aPA_WymhqHYLkE7");
                                        map.put("location1", "1568.459875");
                                        map.put("location2", "454.451236");
                                        map.put("features", "27");
                                        map.put("subUrl", "mtop.mediaplatform.hierarchy.detail");
                                        map.put("urlVer", "1.0");
                                        map.put("timestamp", timestamp);
                                        map.put("data", "{\"accountId\":\"13558937\",\"liveId\":\"283552319336\"}");

                                        XHeader xHeader = new XHeader();

                                        xHeader.setUtdid((String) map.get("utdid"));
                                        xHeader.setUid((String) map.get("uid"));
                                        xHeader.setAppkey((String) map.get("appkey"));
                                        xHeader.setSid((String) map.get("sid"));
                                        xHeader.setTtid((String) map.get("ttid"));
                                        xHeader.setPv((String) map.get("pv"));
                                        xHeader.setDevid((String) map.get("devid"));
                                        xHeader.setLocation1((String) map.get("location1"));
                                        xHeader.setLocation2((String) map.get("location2"));
                                        xHeader.setFeatures((String) map.get("features"));
                                        xHeader.setSubUrl((String) map.get("subUrl"));
                                        xHeader.setUrlVer((String) map.get("urlVer"));
                                        xHeader.setTimestamp(timestamp);
                                        xHeader.setData((String) map.get("data"));

                                        xHeader.setxSign(signs.get("x-sign"));
                                        xHeader.setxSgext(signs.get("x-sgext"));
                                        xHeader.setxMiniWua(signs.get("x-mini-wua"));
                                        xHeader.setxUmt(signs.get("x-umt"));

                                        Map<String, Object> jsonParams = new HashMap<>();
                                        jsonParams.put("liveId", "283552319336");
                                        jsonParams.put("accountId", "13558937");

                                        String jsonText = JSON.toJSONString(jsonParams);

                                        String subUrl = "mtop.mediaplatform.hierarchy.detail";
                                        String url = "https://acs.m.taobao.com/gw/" + subUrl + "/1.0/?data=" + URLEncoder.encode(jsonText);

                                        Response<String> response = HttpHelper.execute(
                                                new SiteConfig()
                                                        .setUserAgent("MTOPSDK%2F3.1.1.7+%28Android%3B5.1.1%3Bsamsung%3BSM-J120F%29")
                                                        .setContentType("application/x-www-form-urlencoded;charset=UTF-8")
                                                        .addHeaders(xHeader.getHeaders()),
                                                new Request("GET", url, ResponseType.TEXT));

                                        if (response.getStatusCode() == HttpStatus.SC_OK) {
                                            String respText = response.getResult();

                                            JsonParser jsonParser = JsonParserFactory.getJsonParser();
                                            Map<String, Object> mapRet = jsonParser.parseMap(respText);

                                            TaobaoReturn taobaoReturn = new TaobaoReturn(mapRet);
                                            if (!taobaoReturn.getErrorMsg().contains("非法请求签名")) {
//                                                System.out.println("第" + (idx + 1) + " 成功");
                                                success.incrementAndGet();
                                                countDownLatch.countDown();
                                                continue;
                                            }
                                        }
                                    }

                                    System.out.println("       第" + (idx + 1) + " 失败");
                                    fail.addAndGet(1);

                                    countDownLatch.countDown();
                                }
                            }

                        } finally {
                        }
                    }
                });
            }
            countDownLatch.await();

            long times = System.currentTimeMillis() - startTime;
            System.out.println("【线程】总共耗时：" + times + "毫秒, SUCCESS: " + success + ", FAIL: " + fail);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
