package highest.taolive.xdata;

import com.fasterxml.jackson.databind.ObjectMapper;
import highest.taolive.xdata.entity.ErrorCodes;
import highest.taolive.xdata.entity.MTopSignParam;
import highest.taolive.xdata.entity.R;
import highest.taolive.xdata.entity.XHeader;
import highest.taolive.xdata.service.CryptoService;
import highest.taolive.xdata.service.MinaService;
import highest.taolive.xdata.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class SignController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private CryptoService cryptoService;

    @Autowired
    private MinaService minaService;

    @PostMapping("/test")
    public R test() {
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

    @PostMapping("/xdata2")
    public R xsign2(@RequestParam(name = "data") String data, @RequestParam(name = "sign") String sign) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            XHeader xHeader = objectMapper.readValue(data, XHeader.class);

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
}
