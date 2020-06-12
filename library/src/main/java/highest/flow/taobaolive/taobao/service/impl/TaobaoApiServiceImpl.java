package highest.flow.taobaolive.taobao.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import highest.flow.taobaolive.common.config.Config;
import highest.flow.taobaolive.common.defines.ErrorCodes;
import highest.flow.taobaolive.common.http.*;
import highest.flow.taobaolive.common.http.cookie.DefaultCookieStorePool;
import highest.flow.taobaolive.common.http.httpclient.HttpClientFactory;
import highest.flow.taobaolive.common.http.httpclient.response.Response;
import highest.flow.taobaolive.common.utils.HFStringUtils;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.taobao.defines.TaobaoAccountState;
import highest.flow.taobaolive.taobao.entity.*;
import highest.flow.taobaolive.taobao.service.TaobaoApiService;
import highest.flow.taobaolive.taobao.service.XSignService;
import org.apache.http.HttpStatus;
import org.apache.http.cookie.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.util.*;

@Service("taobaoApiService")
public class TaobaoApiServiceImpl implements TaobaoApiService {

    @Autowired
    private XSignService xSignService;

    @Override
    public R getUserSimple(TaobaoAccount taobaoAccount) {
        try {
            Map<String, Object> jsonParams = new HashMap<>();
            jsonParams.put("data", "{}");

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonText = objectMapper.writeValueAsString(jsonParams);

            String subUrl = "mtop.user.getusersimple";
            String url = "https://api.m.taobao.com/gw/" + subUrl + "/1.0/?data=" + URLEncoder.encode(jsonText);

            XHeader xHeader = new XHeader(taobaoAccount);
            xHeader.setSubUrl(subUrl);
            xHeader.setUrlVer("1.0");
            xHeader.setData(jsonText);
            xHeader.setXsign(xSignService.sign(xHeader));

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                        .setUserAgent("MTOPSDK/3.1.1.7 (Android;5.1.1)")
                        .addHeaders(xHeader.getHeaders()),
                    new Request("GET", url, ResponseType.TEXT));

            if (response.getStatusCode() != HttpStatus.SC_OK) {
                return null;
            }

            String respText = response.getResult();
            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> map = jsonParser.parseMap(respText);

            TaobaoReturn taobaoReturn = new TaobaoReturn(map);
            if (taobaoReturn.getErrorCode() != ErrorCodes.SUCCESS) {
                return R.error(taobaoReturn.getErrorMsg());
            }

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R GetH5Token(TaobaoAccount taobaoAccount) {
        return null;
    }

    @Override
    public R ParseTaoCode(String taocode) {
        return null;
    }

    @Override
    public R GetLiveDetail(LiveRoom liveRoom) {
        return null;
    }

    @Override
    public R GetLiveProducts(LiveRoom liveRoom, TaobaoAccount taobaoAccount) {
        return null;
    }

    @Override
    public R GetLiveEntry(LiveRoom liveRoom, TaobaoAccount taobaoAccount) {
        return null;
    }

    @Override
    public R TaskFollow(LiveRoom liveRoom, TaobaoAccount taobaoAccount) {
        return null;
    }

    @Override
    public R TaskStay(LiveRoom liveRoom, TaobaoAccount taobaoAccount, int stayMinutes) {
        return null;
    }

    @Override
    public R TaskBuy(LiveRoom liveRoom, TaobaoAccount taobaoAccount, String productId) {
        return null;
    }

    @Override
    public R CreateLiveRoomWeb(PreLiveRoomSpec preLiveRoomSpec, TaobaoAccount taobaoAccount) {
        return null;
    }

    @Override
    public R CreateLiveRoom(PreLiveRoomSpec preLiveRoomSpec, TaobaoAccount taobaoAccount) {
        return null;
    }

    @Override
    public R StartLive(LiveRoom liveRoom, TaobaoAccount taobaoAccount) {
        return null;
    }

    @Override
    public R StopLive(LiveRoom liveRoom, TaobaoAccount taobaoAccount) {
        return null;
    }

    @Override
    public R DeleteLive(LiveRoom liveRoom, TaobaoAccount taobaoAccount) {
        return null;
    }

    @Override
    public R GetProductItemInfo(TaobaoAccount taobaoAccount, String productId) {
        return null;
    }

    @Override
    public R PublishProductWeb(LiveRoom liveRoom, TaobaoAccount taobaoAccount, String productId) {
        return null;
    }

    @Override
    public R OpenProduct(LiveRoom liveRoom, TaobaoAccount taobaoAccount, Product product) {
        return null;
    }

    @Override
    public R AddTimestamp(LiveRoom liveRoom, TaobaoAccount taobaoAccount, String productId) {
        return null;
    }

    @Override
    public R autoLogin(TaobaoAccount taobaoAccount) {
        try {
            R r = this.getUserSimple(taobaoAccount);
            if (r.getCode() == ErrorCodes.SUCCESS) {
                taobaoAccount.setState(TaobaoAccountState.Normal.getState());
                return r;
            }

            ObjectMapper objectMapper = new ObjectMapper();

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
            xHeader.setSubUrl(subUrl);
            xHeader.setUrlVer("1.0");
            xHeader.setData(jsonText);
            xHeader.setXsign(xSignService.sign(xHeader));

            Map<String, String> postParams = new HashMap<>();
            postParams.put("data", URLEncoder.encode(jsonText));

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("MTOPSDK/3.1.1.7 (Android;5.1.1)")
                            .addHeaders(xHeader.getHeaders())
                            .addHeader("Content-Type", "application/x-www-form-urlencoded"),
                    new Request("POST", url, ResponseType.TEXT)
                            .setParameters(postParams),
                    new DefaultCookieStorePool(taobaoAccount.getCookieStore()));
            if (response.getStatusCode() != HttpStatus.SC_OK) {
                taobaoAccount.setState(TaobaoAccountState.AutoLoginFailed.getState());
                return R.error();
            }

            String respText = response.getResult();
            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> map = jsonParser.parseMap(respText);

            TaobaoReturn taobaoReturn = new TaobaoReturn(map);
            if (taobaoReturn.getErrorCode() != ErrorCodes.SUCCESS) {
                taobaoAccount.setState(TaobaoAccountState.AutoLoginFailed.getState());
                return R.error(taobaoReturn.getErrorMsg());
            }

            Map<String, Object> mapData = (Map<String, Object>) map.get("data");
            Map<String, Object> mapReturnValue = (Map<String, Object>) mapData.get("returnValue");
            String data = (String) mapReturnValue.get("data");
            Map<String, Object> mapRetData = jsonParser.parseMap(data);

            String autoLoginToken = (String) mapRetData.get("autoLoginToken");
            long expires = (long) mapRetData.get("expires");
            List<String> lstCookieHeaders = (List<String>) mapRetData.get("cookies");

            List<Cookie> lstCookies = CookieHelper.parseCookieHeaders(url, lstCookieHeaders);

            String sid = (String) mapRetData.get("sid");
            String uid = (String) mapRetData.get("uid");
            String nick = (String) mapRetData.get("nick");

            taobaoAccount.setState(TaobaoAccountState.Normal.getState());

            return R.ok()
                    .put("expires", expires)
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

    @Override
    public R getLoginQRCodeURL() {
        try {
            String url = "https://qrlogin.taobao.com/qrcodelogin/generateNoLoginQRCode.do?lt=m";

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2)")
                            .addHeader("Referer", url)
                            .setContentType("application/x-www-form-urlencoded"),
                    new Request("GET", url, ResponseType.TEXT));
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

            QRCode qrCode = new QRCode();
            String lgToken = (String) map.get("at");
            String qrCodeUrl = (String) map.get("url");
            long timestamp = (long) map.get("t");

            qrCode.setTimestamp(timestamp);
            qrCode.setAccessToken(lgToken);
            qrCode.setNavigateUrl(qrCodeUrl);

            return R.ok()
                    .put("qrCode", qrCode);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R getLoginQRCodeCsrfToken(TaobaoAccount taobaoAccount, QRCode qrCode) {
        try {
            Map<String, Object> jsonParams = new HashMap<>();
            jsonParams.put("_tbScancodeApproach_", "scan");
            jsonParams.put("ttid", "600000@taobao_android_8.7.1");
            jsonParams.put("shortURL", qrCode.getNavigateUrl());

            String url = "https://login.m.taobao.com/qrcodeLogin.htm?";
            for (String key : jsonParams.keySet()) {
                url += key + "=" + URLEncoder.encode((String) jsonParams.get(key)) + "&";
            }

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2)")
                            .setAccept("text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8"),
                    new Request("GET", url, ResponseType.TEXT));
            if (response.getStatusCode() != HttpStatus.SC_OK) {
                return R.error();
            }

            String respText = response.getResult();

            // 获取csrfToken
            int startPos = respText.indexOf("name=\"csrftoken\"");
            if (startPos < 0)
                return null;
            startPos = respText.indexOf("value=\"", startPos);
            if (startPos < 0)
                return null;
            startPos += "value=\"".length();

            int endPos = respText.indexOf("\"", startPos);
            if (endPos < 0)
                return null;
            String csrfToken = respText.substring(startPos, endPos);

            return R.ok()
                    .put("csrfToken", csrfToken);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R authQRCode(TaobaoAccount taobaoAccount, QRCode qrCode, String csrfToken) {
        try {
            String url = "https://login.m.taobao.com/qrcodeLoginAuthor.do?qr_t=s";

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2)")
                            .setContentType("application/x-www-form-urlencoded"),
                    new Request("POST", url, ResponseType.TEXT)
                            .addParameter("csrftoken", csrfToken)
                            .addParameter("shortURL", qrCode.getNavigateUrl())
                            .addParameter("ql", "true"));
            if (response.getStatusCode() != HttpStatus.SC_OK) {
                return R.error();
            }

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R checkLoginByQRCode(TaobaoAccount taobaoAccount, QRCode qrCode) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            Map<String, String> tokenInfo = new HashMap<>();
            tokenInfo.put("utdid", taobaoAccount.getUtdid());
            tokenInfo.put("appName", "25443018");
            tokenInfo.put("token", qrCode.getAccessToken());
            tokenInfo.put("t", String.valueOf(qrCode.getTimestamp()));

            Map<String, String> umidToken = new HashMap<>();
            umidToken.put("umidToken", taobaoAccount.getUmidToken());

            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("tokenInfo", objectMapper.writeValueAsString(tokenInfo));
            paramMap.put("riskControlInfo", objectMapper.writeValueAsString(umidToken));
            paramMap.put("ext", "{}");

            String jsonText = objectMapper.writeValueAsString(paramMap);

            String subUrl = "mtop.taobao.havana.mlogin.qrcodelogin";
            String url = "https://api.m.taobao.com/gw/" + subUrl + "/1.0/?data=" + URLEncoder.encode(jsonText);

            XHeader xHeader = new XHeader(new Date());
            xHeader.setSubUrl(subUrl);
            xHeader.setUrlVer("1.0");
            xHeader.setData(jsonText);
            xHeader.setXsign(xSignService.sign(xHeader));

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2)")
                            .setContentType("application/x-www-form-urlencoded")
                            .addHeaders(xHeader.getHeaders()),
                    new Request("GET", url, ResponseType.TEXT));
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

            Map<String, Object> mapData = (Map<String, Object>) map.get("data");
            Map<String, Object> mapReturnValue = (Map<String, Object>) mapData.get("returnValue");
            String data = (String) mapReturnValue.get("data");
            Map<String, Object> mapRetData = jsonParser.parseMap(data);

            String autoLoginToken = (String) mapRetData.get("autoLoginToken");
            long expires = (long) mapRetData.get("expires");
            List<String> lstCookieHeaders = (List<String>) mapRetData.get("cookies");

            List<Cookie> lstCookies = CookieHelper.parseCookieHeaders(url, lstCookieHeaders);

            String sid = (String) mapRetData.get("sid");
            String uid = (String) mapRetData.get("uid");
            String nick = (String) mapRetData.get("nick");

            return R.ok()
                    .put("expires", expires)
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

    @Override
    public R postpone(TaobaoAccount taobaoAccount) {
        try {
            R r = this.getUserSimple(taobaoAccount);
            if (r.getCode() == ErrorCodes.FAIL_SYS_SESSION_EXPIRED) {
                taobaoAccount.setState(TaobaoAccountState.Expired.getState());
                return r;
            }

            r = this.getLoginQRCodeURL();
            if (r.getCode() != ErrorCodes.SUCCESS) {
                return r;
            }
            QRCode qrCode = (QRCode) r.get("qrCode");
            r = this.getLoginQRCodeCsrfToken(taobaoAccount, qrCode);
            String csrfToken = (String) r.get("csrfToken");
            if (r.getCode() != ErrorCodes.SUCCESS || HFStringUtils.isNullOrEmpty(csrfToken)) {
                taobaoAccount.setState(TaobaoAccountState.AutoLoginFailed.getState());
                return r;
            }

            this.checkLoginByQRCode(taobaoAccount, qrCode);
            r = this.authQRCode(taobaoAccount, qrCode, csrfToken);

            boolean success = r.getCode() == ErrorCodes.SUCCESS ? true : false;
            int retry = 0;
            while (success && retry < Config.MAX_RETRY) {
                r = this.checkLoginByQRCode(taobaoAccount, qrCode);
                if (r.getCode() != ErrorCodes.SUCCESS) {
                    retry++;
                    Thread.sleep(100);
                    continue;
                }

                long expires = (long) r.get("expires");
                String autoLoginToken = (String) r.get("autoLoginToken");
                List<Cookie> lstCookies = (List<Cookie>) r.get("cookie");
                String sid = (String) r.get("sid");
                String uid = (String) r.get("uid");
                String nick = (String) r.get("nick");

                return R.ok()
                        .put("expires", expires)
                        .put("autoLoginToken", autoLoginToken)
                        .put("cookie", lstCookies)
                        .put("sid", sid)
                        .put("uid", uid)
                        .put("nick", nick);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }
}
