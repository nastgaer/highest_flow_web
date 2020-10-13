package highest.taolive.xdata.http;

import com.alibaba.fastjson.JSON;
import highest.taolive.xdata.entity.MTopSignParam;
import highest.taolive.xdata.entity.Packet;
import highest.taolive.xdata.entity.UnifiedSignParam;
import highest.taolive.xdata.entity.XHeader;
import highest.taolive.xdata.service.CryptoService;
import highest.taolive.xdata.service.MinaService;
import highest.taolive.xdata.utils.StringUtils;
import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.protocols.http.request.Method;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.annotation.ExceptionProxy;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SimpleWebServer extends NanoHTTPD {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private CryptoService cryptoService = new CryptoService();

    private boolean quiet = false;

    private RequestContext requestContext;

    public SimpleWebServer(RequestContext requestContext) {
        super(requestContext.getHost(), requestContext.getPort());
        this.requestContext = requestContext;
    }

    public RequestContext getRequestContext() {
        return requestContext;
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
        if (!"25443018".equalsIgnoreCase(appKey)){
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
        if (!"25443018".equalsIgnoreCase(appKey)){
            return false;
        }

        if(!"25443018".equalsIgnoreCase(p3)){
            return false;
        }

        return true;
    }

    @Override
    public Response handle(IHTTPSession session) {
        try {
            Method method = session.getMethod();
            if (Method.POST.equals(method)) {
                try {
                    Map<String, String> files = new HashMap<>();
                    session.parseBody(files);

                    System.out.println(files.size());

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            Map<String, String> header = session.getHeaders();
            Map<String, String> params = session.getParms();
            String uri = session.getUri();

            if (!this.quiet) {
                System.out.println(session.getMethod() + " '" + uri + "' ");

                Iterator<String> e = header.keySet().iterator();
                while (e.hasNext()) {
                    String value = e.next();
                    System.out.println("  HDR: '" + value + "' = '" + header.get(value) + "'");
                }
                e = params.keySet().iterator();
                while (e.hasNext()) {
                    String value = e.next();
                    System.out.println("  PRM: '" + value + "' = '" + params.get(value) + "'");
                }
            }

            String respText = "";
            switch (uri) {
                case "/xdata":
                    respText = onXdata(params);
                    break;
                default:
                    Map<String, Object> map = new HashMap<>();
                    map.put("ret", ErrorCodes.INVALID_URI.getCode());
                    map.put("msg", "INVALID URI");
                    respText = JSON.toJSONString(map);
                    break;
            }

            return Response.newFixedLengthResponse(Status.OK, "application/json", respText);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Response.newFixedLengthResponse(Status.OK, "application/json", "{}");
    }

    private String onXdata(Map<String, String> mapParam) {
        Map<String, Object> resultMap = new HashMap<>();
        if (mapParam == null || mapParam.size() < 1) {
            resultMap.put("ret", ErrorCodes.INVALID_PARAMETER.getCode());
            resultMap.put("msg", "INVALID PARAMETER");
            return JSON.toJSONString(resultMap);
        }

        String data = String.valueOf(mapParam.get("data"));
        String sign = String.valueOf(mapParam.get("sign"));

        boolean verify = cryptoService.verify(data, sign);
        if (!verify) {
            resultMap.put("ret", ErrorCodes.INVALID_PARAMETER.getCode());
            resultMap.put("msg", "INVALID SIGN");
            return JSON.toJSONString(resultMap);
        }

        String plain = cryptoService.decrypt(data);

        try {
            XHeader xHeader = JSON.parseObject(plain, XHeader.class);

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
                    resultMap.put("ret", ErrorCodes.INVALID_PARAMETER.getCode());
                    resultMap.put("msg", "不合法参数");
                    return JSON.toJSONString(resultMap);
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
                    resultMap.put("ret", ErrorCodes.INVALID_PARAMETER.getCode());
                    resultMap.put("msg", "不合法参数");
                    return JSON.toJSONString(resultMap);
                }

                Packet packet = new Packet();
                packet.setProtocol("xsign6.3");
                packet.setObj(unifiedSignParam);

                jsonText = JSON.toJSONString(packet);

            } else {
                resultMap.put("ret", ErrorCodes.INVALID_PARAMETER.getCode());
                resultMap.put("msg", "无效pv");
                return JSON.toJSONString(resultMap);
            }

//            logger.info("<< " + jsonText);
            String respText = MinaService.sendMessage(jsonText);
//            logger.info(">> " + (respText == null ? "null" : respText));

            if (StringUtils.isNullOrEmpty(respText)) {
                resultMap.put("ret", ErrorCodes.INVALID_PARAMETER.getCode());
                resultMap.put("msg", "空了");
                return JSON.toJSONString(resultMap);
            }

            Map<String, Object> map = JSON.parseObject(respText, Map.class);

            int code = (int) map.get("ret");
            String msg = (String) map.get("msg");
            if (code != 100) {
                resultMap.put("ret", ErrorCodes.FAILED.getCode());
                resultMap.put("msg", msg);
                return JSON.toJSONString(resultMap);
            }

            Map<String, Object> mapData = (Map) map.get("data");

            String version = mapData == null || !mapData.containsKey("version") ? "" : String.valueOf(mapData.get("version"));
            String xsign = mapData == null || !mapData.containsKey("x-sign") ? "" : String.valueOf(mapData.get("x-sign"));
            String wua = mapData == null || !mapData.containsKey("wua") ? "" : String.valueOf(mapData.get("wua"));
            String sgext = mapData == null || !mapData.containsKey("x-sgext") ? "" : String.valueOf(mapData.get("x-sgext"));
            String miniWua = mapData == null || !mapData.containsKey("x-mini-wua") ? "" : String.valueOf(mapData.get("x-mini-wua"));
            String umt = mapData == null || !mapData.containsKey("x-umt") ? "" : String.valueOf(mapData.get("x-umt"));

            if (!StringUtils.isNullOrEmpty(xsign)) {
                Map<String, String> mapRet = new HashMap<>();
                mapRet.put("version", version);
                mapRet.put("xsign", xsign);
                mapRet.put("wua", wua);
                mapRet.put("sgext", sgext);
                mapRet.put("miniWua", miniWua);
                mapRet.put("umt", umt);

                resultMap.put("ret", ErrorCodes.SUCCESS.getCode());
                resultMap.put("msg", "成功");
                resultMap.put("data", mapRet);
                return JSON.toJSONString(resultMap);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        resultMap.put("ret", ErrorCodes.FAILED.getCode());
        resultMap.put("msg", "xsign验证失败");
        return JSON.toJSONString(resultMap);
    }
}
