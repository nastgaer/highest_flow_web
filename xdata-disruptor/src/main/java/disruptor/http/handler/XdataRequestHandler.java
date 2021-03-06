package disruptor.http.handler;

import com.alibaba.fastjson.JSON;
import mina.entity.*;
import mina.service.MinaService;
import org.slf4j.LoggerFactory;
import disruptor.http.HTTP;
import disruptor.http.HTTPException;
import disruptor.http.Request;
import disruptor.http.Response;
import xdata.XdataDisruptorApplication;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class XdataRequestHandler extends RequestHandlerInterface {

    public static org.slf4j.Logger logger = LoggerFactory.getLogger(XdataRequestHandler.class);

    public XdataRequestHandler(Request req, Response resp) {
        super(req, resp);
    }

    private R r = null;

    @Override
    public void handleRequest() throws HTTPException, IOException {
        try {
            Map<String, String> param = request.getQueryPairs();

//            logger.info(">> " + JSON.toJSONString(param));
            R r = onXdata(param);
//            logger.info("<< " + JSON.toJSONString(r));

            String jsonText = r == null ? "{}" : JSON.toJSONString(r);

            response.sendStatus(HTTP.OK);
            response.sendBasicHeaders();
            response.sendHeaderEntry("Content-Length: " + jsonText.length());
            response.sendHeaderEntry("Content-Type: application/json; charset=UTF-8");
            response.finishHeaders();

            response.out.write(jsonText);

            if (r.getCode() == ErrorCodes.SUCCESS) {
                XdataDisruptorApplication.success.incrementAndGet();
            } else {
                XdataDisruptorApplication.failed.incrementAndGet();
            }

        } catch (Exception ex) {
            ex.printStackTrace();

            R r = R.error(ex.toString());
            String jsonText = JSON.toJSONString(r);

            response.sendStatus(HTTP.OK);
            response.sendBasicHeaders();
            response.sendHeaderEntry("Content-Length: " + jsonText.length());
            response.sendHeaderEntry("Content-Type: application/json; charset=UTF-8");
            response.finishHeaders();

            response.out.write(jsonText);

            XdataDisruptorApplication.failed.incrementAndGet();
        }
    }

    private R onXdata(Map<String, String> param) {
        String utdid = param.get("utdid");
        String uid = param.get("uid");
        String appkey = param.get("appkey");
        String sid = param.get("sid");
        String ttid = param.get("ttid");
        String pv = param.get("pv");
        String devid = param.get("devid");
        String location1 = param.get("location1");
        String location2 = param.get("location2");
        String features = param.get("features");
        String subUrl = param.get("subUrl");
        String urlVer = param.get("urlVer");
        long timestamp = param.get("timestamp") == null ? 0 : Long.parseLong(param.get("timestamp"));
        String data = param.get("data");

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
                return R.error(ErrorCodes.INVALID_COMMAND, "???????????????");
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
                return R.error(ErrorCodes.INVALID_COMMAND, "???????????????");
            }

            Packet packet = new Packet();
            packet.setProtocol("xsign6.3");
            packet.setObj(unifiedSignParam);

            jsonText = JSON.toJSONString(packet);
        }

        try {
            String respText = MinaService.sendMessage(jsonText);

            if (respText == null || respText.length() < 1) {
                return R.error("??????");
            }

            Map<String, Object> map = JSON.parseObject(respText, Map.class);

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
                return R.error("xsign????????????");
            }

            return R.ok("??????")
                    .put("version", version)
                    .put("xsign", xsign)
                    .put("wua", wua)
                    .put("x-mini-wua", miniWua)
                    .put("x-sgext", sgext)
                    .put("x-umt", umt);

        } catch (Exception ex) {
            return R.error("xsign????????????");
        }
    }

    /**
     * ????????????
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
        //tbzb??? appKey ???????????????
        if(!"25443018".equalsIgnoreCase(appKey)){
            return false;
        }

        if(!"25443018".equalsIgnoreCase(p2)){
            return false;
        }

        return true;
    }

    /**
     * ????????????
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
        //tbzb??? appKey ???????????????
        if(!"25443018".equalsIgnoreCase(appKey)){
            return false;
        }

        if(!"25443018".equalsIgnoreCase(p3)){
            return false;
        }

        return true;
    }
}
