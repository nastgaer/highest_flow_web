package highest.flow.taobaolive.taobao.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import highest.flow.taobaolive.common.http.HttpHelper;
import highest.flow.taobaolive.common.http.SiteConfig;
import highest.flow.taobaolive.common.http.Request;
import highest.flow.taobaolive.common.http.ResponseType;
import highest.flow.taobaolive.common.http.httpclient.response.Response;
import highest.flow.taobaolive.common.utils.CryptoUtils;
import highest.flow.taobaolive.common.utils.HFStringUtils;
import highest.flow.taobaolive.security.service.CryptoService;
import highest.flow.taobaolive.taobao.entity.H5Header;
import highest.flow.taobaolive.taobao.entity.XHeader;
import highest.flow.taobaolive.taobao.service.SignService;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

@Service("signService")
public class SignServiceImpl implements SignService {

    @Autowired
    private CryptoService cryptoService;

    @Value("${sign.url}")
    private String signUrl;

    @Override
    public String xsign(XHeader xHeader) {
        try {
            Map<String, String> map = new HashMap<>();
            map.put("utdid", xHeader.getUtdid());
            map.put("uid", xHeader.getUid());
            map.put("appkey", xHeader.getAppkey());
            map.put("sid", xHeader.getSid());
            map.put("ttid", xHeader.getTtid());
            map.put("pv", xHeader.getPv());
            map.put("devid", xHeader.getDevid());
            map.put("location1", xHeader.getLocation1());
            map.put("location2", xHeader.getLocation2());
            map.put("features", xHeader.getFeatures());
            map.put("subUrl", xHeader.getSubUrl());
            map.put("urlVer", xHeader.getUrlVer());
            map.put("timestamp", String.valueOf(xHeader.getShortTimestamp()));
            map.put("data", xHeader.getData());

            String url = signUrl;

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonText = objectMapper.writeValueAsString(map);
            String encoded = cryptoService.encrypt(jsonText);

            Map<String, String> postParams = new HashMap<>();
            postParams.put("data", encoded);
            postParams.put("sign", cryptoService.sign(encoded));

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
            return null;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public String h5sign(H5Header h5Header, String postData) {
        if (h5Header.isExpired()) {
            return null;
        }

        String plain = h5Header.getToken() + "&" + String.valueOf(h5Header.getLongTimestamp()) + "&" + h5Header.getAppKey() + "&" + postData;
        return CryptoUtils.MD5(plain);
    }
}
