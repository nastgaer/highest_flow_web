package highest.flow.taolive.xdata;

import com.alibaba.fastjson.JSON;
import highest.flow.taolive.xdata.http.HttpHelper;
import highest.flow.taolive.xdata.http.Request;
import highest.flow.taolive.xdata.http.ResponseType;
import highest.flow.taolive.xdata.http.SiteConfig;
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

    @Value("${http.mode:httpclient")
    private String httpMode = "";

    @Value("${http.async:false}")
    private boolean async = false;

    private static OkHttpClient client = null;

    private AtomicInteger success = new AtomicInteger(0), fail = new AtomicInteger(0);

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

    private String callXsign(Map<String, Object> map) {
        try {
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
                                    .setUserAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36")
                                    .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8"),
                            new Request("POST", url, ResponseType.TEXT)
                                    .setParameters(postParams));
                    if (response.getStatusCode() != HttpStatus.SC_OK) {
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

                if (StringUtils.isNotEmpty(xsign) && StringUtils.isNotEmpty(miniWua)) {
                    return xsign;
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
                                    .setUserAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36")
                                    .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8"),
                            new Request("GET", url, ResponseType.TEXT));
                    if (response.getStatusCode() != HttpStatus.SC_OK) {
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

                if (StringUtils.isNotEmpty(xsign) && StringUtils.isNotEmpty(miniWua)) {
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
                            Map<String, Object> map = new HashMap<>();
                            map.put("utdid", "");
                            map.put("uid", "");
                            map.put("appkey", "25443018");
                            map.put("sid", "");
                            map.put("ttid", "10005533@taobaolive_android_1.8.4");
                            map.put("pv", "6.3");
                            map.put("devid", "");
                            map.put("location1", "");
                            map.put("location2", "");
                            map.put("features", "27");
                            map.put("subUrl", "mtop.taobao.sharepassword.querypassword");
                            map.put("urlVer", "1.0");
                            map.put("timestamp", 1599203806);
                            map.put("data", "{\"passwordContent\":\"￥YIJNcWAbEW3￥\"}");

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
                                    String result = callXsign(map);
                                    if (StringUtils.isNotBlank(result)) {
                                        System.out.println("第" + (idx + 1) + " 成功");
                                        success.incrementAndGet();
                                    } else {
                                        System.out.println("第" + (idx + 1) + " 失败: " + result);
                                        fail.addAndGet(1);
                                    }

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
