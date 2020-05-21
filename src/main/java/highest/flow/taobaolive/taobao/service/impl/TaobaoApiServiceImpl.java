package highest.flow.taobaolive.taobao.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import highest.flow.taobaolive.common.defines.ErrorCodes;
import highest.flow.taobaolive.common.http.CookieHelper;
import highest.flow.taobaolive.common.http.HttpHelper;
import highest.flow.taobaolive.common.http.Request;
import highest.flow.taobaolive.common.http.ResponseType;
import highest.flow.taobaolive.common.http.response.Response;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.taobao.entity.TaobaoAccount;
import highest.flow.taobaolive.taobao.entity.TaobaoReturn;
import highest.flow.taobaolive.taobao.entity.XHeader;
import highest.flow.taobaolive.taobao.service.TaobaoApiService;
import org.apache.http.HttpStatus;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.impl.client.BasicCookieStore;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("taobaoApiService")
public class TaobaoApiServiceImpl implements TaobaoApiService {

    @Override
    public R GetUserSimple(TaobaoAccount taobaoAccount) {
        return null;
    }

    @Override
    public R AutoLogin(TaobaoAccount taobaoAccount) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            Map<String, Object> apiReferMap = new HashMap<>();
            apiReferMap.put("apiName", "mtop.amp.ampService.getRecentContactsOfficialList");
            apiReferMap.put("appBackGround", false);
            apiReferMap.put("eventName", "SESSION_INVALID");
            apiReferMap.put("long_nick", "");
            apiReferMap.put("msgCode", "FAIL_SYS_SESSION_EXPIRED");
            apiReferMap.put("processName", "FAIL_SYS_SESSION_EXPIRED");
            apiReferMap.put("v", "4.0");

            Map<String, Object> extMap = new HashMap<>();
            extMap.put("apiRefer", objectMapper.writeValueAsString(apiReferMap));

            Map<String, Object> tokenInfo = new HashMap<>();
            apiReferMap.put("appName", "25443018");
            apiReferMap.put("appVersion", "android_7.6.0");
            apiReferMap.put("deviceId", taobaoAccount.getDevid());
            apiReferMap.put("deviceName", "");
            apiReferMap.put("locale", "zh_CN");
            apiReferMap.put("sdkVersion", "android_3.8.1");
            apiReferMap.put("site", 0);
            apiReferMap.put("t", new Date().getTime());
            apiReferMap.put("token", taobaoAccount.getAutoLoginToken());
            apiReferMap.put("ttid", "600000@taobao_android_7.6.0");
            apiReferMap.put("useAcitonType", true);
            apiReferMap.put("useDeviceToken", true);

            Map<String, Object> umidTokenMap = new HashMap<>();
            umidTokenMap.put("umidToken", taobaoAccount.getUmidToken());

            Map<String, Object> jsonParams = new HashMap<>();
            jsonParams.put("ext", objectMapper.writeValueAsString(extMap));
            jsonParams.put("userId", "0");
            jsonParams.put("tokenInfo", objectMapper.writeValueAsString(tokenInfo));
            jsonParams.put("riskControlInfo", objectMapper.writeValueAsString(umidTokenMap));

            String jsonText = objectMapper.writeValueAsString(jsonParams);

            String subUrl = "com.taobao.mtop.mloginunitservice.autologin";
            String url = "https://acs.m.taobao.com/gw/" + subUrl + "/1.0/";

            XHeader xHeader = new XHeader(taobaoAccount);

            Request request = new Request("GET", url, ResponseType.TEXT);
            request.setUserAgent("MTOPSDK/3.1.1.7 (Android;5.1.1)");
            request.setHeaders(xHeader.getHeaders());

            Map<String, String> postParams = new HashMap<>();
            postParams.put("data", URLEncoder.encode(jsonText));
            request.setParameters(postParams);

            request.setCookieStore(taobaoAccount.getCookieStore());

            HttpHelper httpHelper = new HttpHelper();
            Response<String> response = httpHelper.execute(request);
            if (response.getStatusCode() != HttpStatus.SC_OK) {
                return R.error();
            }

            String respText = response.getResult();
            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> map = jsonParser.parseMap(respText);

            TaobaoReturn taobaoReturn = new TaobaoReturn(map);
            if (taobaoReturn.getErrorCode() != ErrorCodes.SUCCESS) {
                return R.error(taobaoReturn.getErrorMsg());
            }

            Map<String, Object> mapData = (Map<String, Object>)map.get("data");
            Map<String, Object> mapReturnValue = (Map<String, Object>)mapData.get("returnValue");
            String data = (String)mapReturnValue.get("data");
            Map<String, Object> mapRetData = jsonParser.parseMap(data);

            String autoLoginToken = (String)mapRetData.get("autoLoginToken");
            List<String> lstCookieHeaders = (List<String>)mapRetData.get("cookies");

            List<Cookie> lstCookies = CookieHelper.parseCookieHeaders(url, lstCookieHeaders);
//            CookieStore cookieStore = new BasicCookieStore();
//            if (lstCookies != null) {
//                for (Cookie cookie : lstCookies) {
//                    cookieStore.addCookie(cookie);
//                }
//            }

            String sid = (String)mapRetData.get("sid");
            String uid = (String)mapRetData.get("uid");
            String nick = (String)mapRetData.get("nick");

            return R.ok()
                    .put("autoLoginToken", autoLoginToken)
                    .put("cookie", lstCookies)
                    .put("sid", sid)
                    .put("uid", uid)
                    .put("nick", nick);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }


}
