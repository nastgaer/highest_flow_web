package highest.taolive.xdata;

import com.fasterxml.jackson.databind.ObjectMapper;
import highest.taolive.xdata.entity.*;
import highest.taolive.xdata.service.CryptoService;
import highest.taolive.xdata.service.MinaService;
import highest.taolive.xdata.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class SignController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private CryptoService cryptoService;

    @Autowired
    private MinaService minaService;

    @GetMapping("/test")
    public R test() {
        return R.ok();
    }

    @PostMapping("/test")
    public R test1(@RequestParam("content") String content) {
        return R.ok();
    }

    @PostMapping("/xdata")
    public R xsign(@RequestParam(name = "data") String data, @RequestParam(name = "sign") String sign) {
        try {
            boolean verify = cryptoService.verify(data, sign);
            if (!verify) {
                return R.error("参数验证失败");
            }

            String plain = cryptoService.decrypt(data);


            ObjectMapper objectMapper = new ObjectMapper();
            XHeader xHeader = objectMapper.readValue(plain, XHeader.class);

            String pv = xHeader.getPv();

            String jsonText = "";
            if (pv.compareTo("6.2") == 0) {
                // Convert XHeader to MTopSignParam
                MTopSignParam mTopSignParam = new MTopSignParam();

                Map<String, String> p1 = new HashMap<>();
                p1.put("utdid", xHeader.getUtdid());
                p1.put("uid", xHeader.getUid());
                p1.put("appKey", xHeader.getAppkey());
                p1.put("pv", xHeader.getPv());
                p1.put("t", String.valueOf(xHeader.getTimestamp()));
                p1.put("sid", xHeader.getSid());
                p1.put("ttid", xHeader.getTtid());
                p1.put("deviceId", xHeader.getDevid());
                p1.put("location", xHeader.getLocation1() + "," + xHeader.getLocation2());
                p1.put("x-features", xHeader.getFeatures());
                p1.put("v", xHeader.getUrlVer());
                p1.put("api", xHeader.getSubUrl());
                p1.put("data", xHeader.getData());

                mTopSignParam.setP1(p1);
                mTopSignParam.setP2("25443018");

                if (!checkMTopSignParam(mTopSignParam)) {
                    return R.error(ErrorCodes.INVALID_PARAMETER, "不合法参数");
                }

                Packet packet = new Packet();
                packet.setProtocol("xsign6.2");
                packet.setObj(mTopSignParam);

                jsonText = objectMapper.writeValueAsString(packet);

            } else if (pv.compareTo("6.3") == 0) {
                UnifiedSignParam unifiedSignParam = new UnifiedSignParam();

                Map<String, String> p1 = new HashMap<>();
                p1.put("deviceId", xHeader.getDevid());
                p1.put("appKey", xHeader.getAppkey());
                p1.put("utdid", xHeader.getUtdid());
                p1.put("x-features", xHeader.getFeatures());
                p1.put("ttid", xHeader.getTtid());
                p1.put("v", xHeader.getUrlVer());
                p1.put("sid", xHeader.getSid());
                p1.put("t", String.valueOf(xHeader.getTimestamp()));
                p1.put("api", xHeader.getSubUrl());
                p1.put("data", xHeader.getData());
                p1.put("uid", xHeader.getUid());

                unifiedSignParam.setP1(p1);

                Map<String, String> p2 = new HashMap<>();
                unifiedSignParam.setP2(p2);

                unifiedSignParam.setP3("25443018");
                unifiedSignParam.setP4(null);
                unifiedSignParam.setP5(false);

                if (!checkUnifiedSignParam(unifiedSignParam)) {
                    return R.error(ErrorCodes.INVALID_PARAMETER, "不合法参数");
                }

                Packet packet = new Packet();
                packet.setProtocol("xsign6.3");
                packet.setObj(unifiedSignParam);

                jsonText = objectMapper.writeValueAsString(packet);

            } else {
                return R.error("无效pv");
            }

//            logger.info("<< " + jsonText);
            String respText = minaService.sendMessage(jsonText);
//            logger.info(">> " + (respText == null ? "null" : respText));

            if (StringUtils.isNullOrEmpty(respText)) {
                return R.error("空了");
            }

            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> map = jsonParser.parseMap(respText);

            int code = (int) map.get("ret");
            String msg = (String) map.get("msg");
            if (code != 100) {
                return R.error(msg);
            }

            Map<String, Object> mapData = (Map) map.get("data");

            String version = mapData == null || !mapData.containsKey("version") ? "" : String.valueOf(mapData.get("version"));
            String xsign = mapData == null || !mapData.containsKey("x-sign") ? "" : String.valueOf(mapData.get("x-sign"));
            String wua = mapData == null || !mapData.containsKey("wua") ? "" : String.valueOf(mapData.get("wua"));
            String sgext = mapData == null || !mapData.containsKey("x-sgext") ? "" : String.valueOf(mapData.get("x-sgext"));
            String miniWua = mapData == null || !mapData.containsKey("x-mini-wua") ? "" : String.valueOf(mapData.get("x-mini-wua"));
            String umt = mapData == null || !mapData.containsKey("x-umt") ? "" : String.valueOf(mapData.get("x-umt"));

            if (!StringUtils.isNullOrEmpty(xsign)) {
                return R.ok("成功")
                        .put("version", version)
                        .put("xsign", xsign)
                        .put("wua", wua)
                        .put("x-mini-wua", miniWua)
                        .put("x-sgext", sgext)
                        .put("x-umt", umt);
            }

            return R.error("xsign验证失败");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error("xsign验证失败");
    }

    @GetMapping("/xdata2")
    public R xsign2(@RequestParam(name = "utdid") String utdid,
                    @RequestParam(name = "uid") String uid,
                    @RequestParam(name = "appkey") String appkey,
                    @RequestParam(name = "sid") String sid,
                    @RequestParam(name = "ttid") String ttid,
                    @RequestParam(name = "pv") String pv,
                    @RequestParam(name = "devid") String devid,
                    @RequestParam(name = "location1") String location1,
                    @RequestParam(name = "location2") String location2,
                    @RequestParam(name = "features") String features,
                    @RequestParam(name = "subUrl") String subUrl,
                    @RequestParam(name = "urlVer") String urlVer,
                    @RequestParam(name = "timestamp") long timestamp,
                    @RequestParam(name = "data") String data) {
        try {
            XHeader xHeader = new XHeader();
            xHeader.setUtdid(utdid);
            xHeader.setUid(uid);
            xHeader.setAppkey(appkey);
            xHeader.setSid(sid);
            xHeader.setTtid(ttid);
            xHeader.setPv(pv);
            xHeader.setDevid(devid);
            xHeader.setLocation1(location1);
            xHeader.setLocation2(location2);
            xHeader.setFeatures(features);
            xHeader.setSubUrl(subUrl);
            xHeader.setUrlVer(urlVer);
            xHeader.setTimestamp(timestamp);
            xHeader.setData(data);

            // Convert XHeader to MTopSignParam
            MTopSignParam mTopSignParam = new MTopSignParam();

            Map<String, String> p1 = new HashMap<>();
            p1.put("utdid", xHeader.getUtdid());
            p1.put("uid", xHeader.getUid());
            p1.put("appKey", xHeader.getAppkey());
            p1.put("pv", xHeader.getPv());
            p1.put("t", String.valueOf(xHeader.getTimestamp()));
            p1.put("sid", xHeader.getSid());
            p1.put("ttid", xHeader.getTtid());
            p1.put("deviceId", xHeader.getDevid());
            p1.put("location", xHeader.getLocation1() + "," + xHeader.getLocation2());
            p1.put("x-features", xHeader.getFeatures());
            p1.put("v", xHeader.getUrlVer());
            p1.put("api", xHeader.getSubUrl());
            p1.put("data", xHeader.getData());

            mTopSignParam.setP1(p1);
            mTopSignParam.setP2("25443018");

            if (!checkMTopSignParam(mTopSignParam)) {
                return R.error(ErrorCodes.INVALID_PARAMETER, "不合法参数");
            }

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonText = objectMapper.writeValueAsString(mTopSignParam);

//            logger.info("<< " + jsonText);
            String respText = minaService.sendMessage(jsonText);
//            logger.info(">> " + (respText == null ? "null" : respText));

            if (StringUtils.isNullOrEmpty(respText)) {
                return R.error("空了");
            }

            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> map = jsonParser.parseMap(respText);
            Map<String, Object> mapData = (Map) map.get("data");

            String xsign = mapData == null || !mapData.containsKey("x-sign") ? "" : String.valueOf(mapData.get("x-sign"));
            String miniWua = mapData == null || !mapData.containsKey("mini-wua") ? "" : String.valueOf(mapData.get("mini-wua"));

            if (!StringUtils.isNullOrEmpty(xsign)) {
                return R.ok("成功").put("xsign", xsign).put("encoded", false).put("mini-wua", miniWua);
            }

            return R.error("xsign验证失败");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error("xsign验证失败");
    }

    /**
     * 检查参数
     * @param mTopSignParam
     * @return
     */
    private boolean checkMTopSignParam(MTopSignParam mTopSignParam) {
        if(mTopSignParam == null){
            return false;
        }
        Map<String, String> p1 = mTopSignParam.getP1();
        if(p1 == null){
            return false;
        }

        String appKey = p1.get("appKey");
        String p2 = mTopSignParam.getP2();
        //tbzb的 appKey 固定写死的
        if(!"25443018".equalsIgnoreCase(appKey)){
            return false;
        }

        if(!"25443018".equalsIgnoreCase(p2)){
            return false;
        }

        return true;
    }

    /**
     * 检查参数
     * @param unifiedSignParam
     * @return
     */
    private boolean checkUnifiedSignParam(UnifiedSignParam unifiedSignParam) {
        if(unifiedSignParam == null){
            return false;
        }
        Map<String, String> p1 = unifiedSignParam.getP1();
        if(p1 == null){
            return false;
        }

        String appKey = p1.get("appKey");
        String p3 = unifiedSignParam.getP3();
        //tbzb的 appKey 固定写死的
        if(!"25443018".equalsIgnoreCase(appKey)){
            return false;
        }

        if(!"25443018".equalsIgnoreCase(p3)){
            return false;
        }

        return true;
    }

    @GetMapping("/mini-wua")
    public R miniwua() {
        try {
            Packet packet = new Packet();
            packet.setProtocol("mini-wua");

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonText = objectMapper.writeValueAsString(packet);

            String respText = minaService.sendMessage(jsonText);

            if (StringUtils.isNullOrEmpty(respText)) {
                return R.error("空了");
            }

            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> map = jsonParser.parseMap(respText);

            int code = (int) map.get("ret");
            String msg = (String) map.get("msg");
            if (code != 100) {
                return R.error(msg);
            }

            Map<String, Object> mapData = (Map) map.get("data");
            String miniWua = mapData == null ? "" : (String) mapData.get("mini-wua");

            if (StringUtils.isNullOrEmpty(miniWua)) {
                return R.error("mini-wua计算失败");
            }

            return R.ok("成功")
                    .put("mini-wua", miniWua);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error("mini-wua验证失败");
    }

    /**
     * 生成com.taobao.accs.net.SpdyConnection.buildAuthUrl的网址的时候需要计算app-sign
     * @param appkey
     * @param utdid
     * @return
     */
    @GetMapping("/app-sign")
    public R appSign(@RequestParam("appkey") String appkey,
                     @RequestParam("utdid") String utdid) {
        try {
            if (!appkey.equalsIgnoreCase("25443018")) {
                return R.error(ErrorCodes.INVALID_PARAMETER, "不合法参数");
            }

            Map<String, String> mapParam = new HashMap<>();
            mapParam.put("appkey", appkey);
            mapParam.put("utdid", utdid);

            Packet packet = new Packet();
            packet.setProtocol("app-sign");
            packet.setObj(mapParam);

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonText = objectMapper.writeValueAsString(packet);

            String respText = minaService.sendMessage(jsonText);

            if (StringUtils.isNullOrEmpty(respText)) {
                return R.error("空了");
            }

            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> map = jsonParser.parseMap(respText);

            int code = (int) map.get("ret");
            String msg = (String) map.get("msg");
            if (code != 100) {
                return R.error(msg);
            }

            Map<String, Object> mapData = (Map) map.get("data");
            String appSign = mapData == null ? "" : (String) mapData.get("app-sign");

            if (StringUtils.isNullOrEmpty(appSign)) {
                return R.error("app-sign计算失败");
            }

            return R.ok("成功")
                    .put("app-sign", appSign);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error("mini-wua验证失败");
    }

    @GetMapping("/navigate")
    public R navigate(@RequestParam("url") String url,
                      @RequestParam("stay") int stayTime) {
        try {
            Map<String, Object> mapParam = new HashMap<>();
            mapParam.put("url", url);
            mapParam.put("stayTime", stayTime);

            Packet packet = new Packet();
            packet.setProtocol("navigate");
            packet.setObj(mapParam);

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonText = objectMapper.writeValueAsString(packet);

            String respText = minaService.sendMessage(jsonText);

            if (StringUtils.isNullOrEmpty(respText)) {
                return R.error("空了");
            }

            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> map = jsonParser.parseMap(respText);

            int code = (int) map.get("ret");
            String msg = (String) map.get("msg");
            if (code != 100) {
                return R.error(msg);
            }

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error("跳转失败");
    }

    @PostMapping("/set-account")
    public R navigate(@RequestParam("utdid") String utdid,
                      @RequestParam("uid") String uid,
                      @RequestParam("sid") String sid,
                      @RequestParam("nick") String nick,
                      @RequestParam("deviceId") String deviceId,
                      @RequestParam("umidToken") String umidToken,
                      @RequestParam(name = "cookies[]") String[] cookies) {
        try {
            List<String> cookieList = new ArrayList<>();
            for (String cookie : cookies) {
                cookieList.add(cookie);
            }

            Map<String, Object> mapParam = new HashMap<>();
            mapParam.put("utdid", utdid);
            mapParam.put("uid", uid);
            mapParam.put("sid", sid);
            mapParam.put("nick", nick);
            mapParam.put("deviceId", deviceId);
            mapParam.put("umidToken", umidToken);
            mapParam.put("cookies", cookieList);

            Packet packet = new Packet();
            packet.setProtocol("set-account");
            packet.setObj(mapParam);

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonText = objectMapper.writeValueAsString(packet);

            String respText = minaService.sendMessage(jsonText);

            if (StringUtils.isNullOrEmpty(respText)) {
                return R.error("空了");
            }

            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> map = jsonParser.parseMap(respText);

            int code = (int) map.get("ret");
            String msg = (String) map.get("msg");
            if (code != 100) {
                return R.error(msg);
            }

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error("跳转失败");
    }

    @GetMapping("/get-content")
    public R getContent() {
        try {
            Packet packet = new Packet();
            packet.setProtocol("get-content");
            packet.setObj(null);

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonText = objectMapper.writeValueAsString(packet);

            minaService.sendMessage(jsonText);

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error("获取内容");
    }

    @PostMapping("/upload-request")
    public R uploadRequest(@RequestParam("content") String content) {
        try {
            System.out.println("request=" + content);

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error("上传请求内容失败");
    }

    @GetMapping("/umid-token")
    public R getUmidToken() {
        try {
            Packet packet = new Packet();
            packet.setProtocol("umtid");
            packet.setObj(null);

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonText = objectMapper.writeValueAsString(packet);

            String respText = minaService.sendMessage(jsonText);

            if (StringUtils.isNullOrEmpty(respText)) {
                return R.error("空了");
            }

            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> map = jsonParser.parseMap(respText);

            int code = (int) map.get("ret");
            String msg = (String) map.get("msg");
            if (code != 100) {
                return R.error(msg);
            }

            Map<String, Object> mapData = (Map) map.get("data");
            String umidToken = mapData == null ? "" : (String) mapData.get("umidtoken");

            if (StringUtils.isNullOrEmpty(umidToken)) {
                return R.error("计算umidToken失败");
            }

            return R.ok("成功")
                    .put("umid-token", umidToken);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error("获取内容");
    }
}
