package com.useful.server;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.useful.common.protobuf.Command;
import com.useful.common.protobuf.Message;
import com.useful.server.entity.*;
import com.useful.server.session.SessionManager;
import com.useful.server.sync.PromiseFuture;
import com.useful.server.sync.WaitingPool;
import com.useful.server.typeServer.TCPServer;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class SignController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private TCPServer tcpServer;

    private AtomicLong sequenceNumber = new AtomicLong(0);

    @Value("${socket.TIMEOUT}")
    private int socketTimeout;

    @GetMapping("/data")
    public R get() {
        return R.ok();
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
                    return R.error(ErrorCodes.INVALID_COMMAND, "不合法参数");
                }

                Packet packet = new Packet();
                packet.setProtocol("xsign6.2");
                packet.setObj(mTopSignParam);

                jsonText = JSON.toJSONString(packet);

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
                    return R.error(ErrorCodes.INVALID_COMMAND, "不合法参数");
                }

                Packet packet = new Packet();
                packet.setProtocol("xsign6.3");
                packet.setObj(unifiedSignParam);

                jsonText = JSON.toJSONString(packet);
            }

            Map.Entry<String, ChannelHandlerContext> entry = SessionManager.random();
            if (entry == null) {
                return R.error(ErrorCodes.NO_CLIENT, "签名计算机器掉线了");
            }

            long messageId = sequenceNumber.incrementAndGet();

            entry.getValue().writeAndFlush(Message.MessageBase.newBuilder()
                    .setMessageId(messageId)
                    .setClientId(entry.getKey())
                    .setCommandType(Command.CommandType.REQUEST)
                    .setData(jsonText));

            PromiseFuture<String> future = new PromiseFuture<String>();
            WaitingPool.getInstance().add(messageId, future);

            try {
                String respText = future.get(socketTimeout, TimeUnit.SECONDS);

                if (respText == null || respText.length() < 1) {
                    return R.error("空了");
                }

                JsonParser jsonParser = JsonParserFactory.getJsonParser();
                Map<String, Object> map = jsonParser.parseMap(respText);

                int code = (int) map.get("ret");
                String msg = (String) map.get("msg");
                if (code != ErrorCodes.SUCCESS.getCode()) {
                    return R.error(msg);
                }

                Map<String, Object> mapData = (Map) map.get("data");

                String version = mapData == null || !mapData.containsKey("version") ? "" : String.valueOf(mapData.get("version"));
                String xsign = mapData == null || !mapData.containsKey("x-sign") ? "" : String.valueOf(mapData.get("x-sign"));
                String wua = mapData == null || !mapData.containsKey("wua") ? "" : String.valueOf(mapData.get("wua"));
                String sgext = mapData == null || !mapData.containsKey("x-sgext") ? "" : String.valueOf(mapData.get("x-sgext"));
                String miniWua = mapData == null || !mapData.containsKey("x-mini-wua") ? "" : String.valueOf(mapData.get("x-mini-wua"));
                String umt = mapData == null || !mapData.containsKey("x-umt") ? "" : String.valueOf(mapData.get("x-umt"));

                if (xsign == null || xsign.length() < 1) {
                    return R.error("xsign验证失败");
                }

                return R.ok("成功")
                        .put("version", version)
                        .put("xsign", xsign)
                        .put("wua", wua)
                        .put("x-mini-wua", miniWua)
                        .put("x-sgext", sgext)
                        .put("x-umt", umt);

            } catch (TimeoutException ex) {
                return R.error("xsign验证失败");
            }

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
}
