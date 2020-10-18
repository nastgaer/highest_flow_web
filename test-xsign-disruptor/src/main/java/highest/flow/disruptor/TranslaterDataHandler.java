package highest.flow.disruptor;

import com.lmax.disruptor.EventHandler;
import highest.flow.entity.TranslatorData;
import highest.flow.taolive.xdata.http.HttpHelper;
import highest.flow.taolive.xdata.http.Request;
import highest.flow.taolive.xdata.http.ResponseType;
import highest.flow.taolive.xdata.http.SiteConfig;
import highest.flow.taolive.xdata.http.httpclient.response.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;

import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

public class TranslaterDataHandler implements EventHandler<TranslatorData> {

    private AtomicLong success, failed;

    private CountDownLatch countDownLatch;

    private String signUrl;

    public CountDownLatch getCountDownLatch() {
        return countDownLatch;
    }

    public void setCountDownLatch(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }

    public String getSignUrl() {
        return signUrl;
    }

    public void setSignUrl(String signUrl) {
        this.signUrl = signUrl;
    }

    public void setAtomic(AtomicLong success, AtomicLong failed) {
        this.success = success;
        this.failed = failed;
    }

    @Override
    public void onEvent(TranslatorData translatorData, long l, boolean b) throws Exception {
        String producerId = translatorData.getProductId();
        int index = translatorData.getIndex();
        Map<String, Object> map = translatorData.getData();

//        try {
//            Thread.sleep(10);
//
//            System.out.println("[" + producerId + "]-[" + consumerId + "] 第" + (index + 1) + " 成功");
//
//            countDownLatch.countDown();
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }

        String url = signUrl + "?";

        for (String key : map.keySet()) {
            url += key + "=" + URLEncoder.encode(String.valueOf(map.get(key))) + "&";
        }

        Response<String> response = HttpHelper.execute(
                new SiteConfig()
                        .setMaxConnTotal(6000)
                        .setMaxConnPerRoute(1000)
                        .setUserAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36")
                        .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8"),
                new Request("GET", url, ResponseType.TEXT));

        if (response.getStatusCode() != HttpStatus.SC_OK) {
            System.out.println("ERROR OCCURRED: " + response.getStatusCode());
            System.out.println("[" + producerId + "] 第" + (index + 1) + " 失败");

            failed.incrementAndGet();
            countDownLatch.countDown();
            return;
        }

        String respText = response.getResult();

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
            success.incrementAndGet();
            System.out.println("[" + producerId + "] 第" + (index + 1) + " 成功");
            countDownLatch.countDown();
            return;
        }

        failed.incrementAndGet();
        System.out.println("[" + producerId + "] 第" + (index + 1) + " 失败");
        countDownLatch.countDown();
    }
}
