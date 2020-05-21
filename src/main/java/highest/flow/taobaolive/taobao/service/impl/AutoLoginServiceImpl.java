package highest.flow.taobaolive.taobao.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import highest.flow.taobaolive.common.defines.ErrorCodes;
import highest.flow.taobaolive.common.http.HttpHelper;
import highest.flow.taobaolive.common.http.Request;
import highest.flow.taobaolive.common.http.ResponseType;
import highest.flow.taobaolive.common.http.response.Response;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.taobao.entity.TaobaoReturn;
import highest.flow.taobaolive.taobao.entity.XHeader;
import highest.flow.taobaolive.taobao.entity.TaobaoAccount;
import highest.flow.taobaolive.taobao.service.AutoLoginService;
import highest.flow.taobaolive.taobao.service.TaobaoApiService;
import highest.flow.taobaolive.taobao.service.XSignService;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

@Service("autoLoginService")
public class AutoLoginServiceImpl implements AutoLoginService {

    @Autowired
    private TaobaoApiService taobaoApiService;

    @Override
    public R autoLogin(TaobaoAccount taobaoAccount) {
        R r = taobaoApiService.GetUserSimple(taobaoAccount);
        if (r.getCode() == ErrorCodes.SUCCESS) {
            return R.ok();
        }

        return R.error();

//        ObjectMapper objectMapper = new ObjectMapper();
//
//        try {
//            Map<String, Object> jsonParams = new HashMap<>();
//            jsonParams.put("ext", objectMapper.writeValueAsString(null));
//            jsonParams.put("userId", "0");
//            jsonParams.put("tokenInfo", objectMapper.writeValueAsString(null));
//            jsonParams.put("riskControlInfo", objectMapper.writeValueAsString(null));
//
//            String jsonText = objectMapper.writeValueAsString(jsonParams);
//
//            String subUrl = "com.taobao.mtop.mloginunitservice.autologin";
//            String url = "https://acs.m.taobao.com/gw/" + subUrl + "/1.0/";
//
//            XHeader xHeader = new XHeader(taobaoAccount);
//
//            Request request = new Request("GET", url, ResponseType.TEXT);
//            request.setUserAgent("MTOPSDK/3.1.1.7 (Android;5.1.1)");
//            request.setHeaders(xHeader.getHeaders());
//
//            Map<String, String> postParams = new HashMap<>();
//            postParams.put("data", URLEncoder.encode(jsonText));
//            request.setParameters(postParams);
//
//            request.setCookieStore(taobaoAccount.getCookieStore());
//
//            HttpHelper httpHelper = new HttpHelper();
//            Response<String> response = httpHelper.execute(request);
//            if (response.getStatusCode() != HttpStatus.SC_OK) {
//                return false;
//            }
//
//            String respText = response.getResult();
//            JsonParser jsonParser = JsonParserFactory.getJsonParser();
//            Map<String, Object> map = jsonParser.parseMap(respText);
//
//            TaobaoReturn taobaoReturn = new TaobaoReturn(map);
//            if (taobaoReturn.getErrorCode() == ErrorCodes.SUCCESS) {
//
//            }
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//
//        return false;
    }

    @Override
    public boolean postPone(TaobaoAccount taobaoAccount) {
        return false;
    }
}
