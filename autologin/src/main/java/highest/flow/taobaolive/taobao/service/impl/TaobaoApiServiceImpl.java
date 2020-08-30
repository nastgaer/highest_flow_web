package highest.flow.taobaolive.taobao.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import highest.flow.taobaolive.common.config.Config;
import highest.flow.taobaolive.common.defines.ErrorCodes;
import highest.flow.taobaolive.common.http.*;
import highest.flow.taobaolive.common.http.cookie.DefaultCookieStorePool;
import highest.flow.taobaolive.common.http.httpclient.response.Response;
import highest.flow.taobaolive.common.utils.CommonUtils;
import highest.flow.taobaolive.common.utils.HFStringUtils;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.taobao.defines.TaobaoAccountState;
import highest.flow.taobaolive.taobao.entity.*;
import highest.flow.taobaolive.taobao.service.TaobaoApiService;
import highest.flow.taobaolive.taobao.service.SignService;
import highest.flow.taobaolive.taobao.utils.DeviceUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.util.*;

@Service("taobaoApiService")
public class TaobaoApiServiceImpl implements TaobaoApiService {

    @Autowired
    private SignService signService;

    @Override
    public R getUserSimple(TaobaoAccountEntity taobaoAccountEntity) {
        try {
            Map<String, Object> jsonParams = new HashMap<>();
            jsonParams.put("data", "{}");

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonText = objectMapper.writeValueAsString(jsonParams);

            String subUrl = "mtop.user.getusersimple";
            String url = "https://api.m.taobao.com/gw/" + subUrl + "/1.0/?data=" + URLEncoder.encode(jsonText);

            XHeader xHeader = new XHeader(taobaoAccountEntity);
            xHeader.setSubUrl(subUrl);
            xHeader.setUrlVer("1.0");
            xHeader.setData(jsonText);
            xHeader.setXsign("ab24260090aaa8c2f96e2358c705f6e9d368f3f08ae4ee8b79"); // signService.xsign(xHeader));

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2)")
                            .addHeaders(xHeader.getHeaders()),
                    new Request("GET", url, ResponseType.TEXT),
                    new DefaultCookieStorePool(taobaoAccountEntity.getCookieStore()));

            if (response.getStatusCode() != HttpStatus.SC_OK) {
                return R.error();
            }

            String respText = response.getResult();
            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> map = jsonParser.parseMap(respText);

            TaobaoReturn taobaoReturn = new TaobaoReturn(map);
            if (taobaoReturn.getErrorCode() != ErrorCodes.SUCCESS) {
                return R.error(taobaoReturn.getErrorCode(), taobaoReturn.getErrorMsg());
            }

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R autoLogin(TaobaoAccountEntity taobaoAccountEntity) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            Map<String, Object> apiReferMap = new HashMap<>();
            apiReferMap.put("apiName", "mtop.amp.ampService.getRecentContactsOfficialList");
            apiReferMap.put("appBackGround", false);
            apiReferMap.put("eventName", "SESSION_INVALID");
            apiReferMap.put("long_nick", "");
            apiReferMap.put("msgCode", "FAIL_SYS_SESSION_EXPIRED");
            apiReferMap.put("processName", "com.taobao.taobao");
            apiReferMap.put("v", "4.0");

            Map<String, Object> extMap = new HashMap<>();
            extMap.put("apiRefer", objectMapper.writeValueAsString(apiReferMap));

            Map<String, Object> tokenInfo = new HashMap<>();
            tokenInfo.put("appName", "25443018");
            tokenInfo.put("appVersion", "android_7.6.0");
            tokenInfo.put("deviceId", taobaoAccountEntity.getDevid());
            tokenInfo.put("deviceName", "");
            tokenInfo.put("locale", "zh_CN");
            tokenInfo.put("sdkVersion", "android_3.8.1");
            tokenInfo.put("site", 0);
            tokenInfo.put("t", new Date().getTime());
            tokenInfo.put("token", taobaoAccountEntity.getAutoLoginToken());
            tokenInfo.put("ttid", "600000@taobao_android_7.6.0");
            tokenInfo.put("useAcitonType", true);
            tokenInfo.put("useDeviceToken", true);

            Map<String, Object> umidTokenMap = new HashMap<>();
            umidTokenMap.put("umidToken", taobaoAccountEntity.getUmidToken());

            Map<String, Object> jsonParams = new HashMap<>();
            jsonParams.put("ext", objectMapper.writeValueAsString(extMap));
            jsonParams.put("userId", "0");
            jsonParams.put("tokenInfo", objectMapper.writeValueAsString(tokenInfo));
            jsonParams.put("riskControlInfo", objectMapper.writeValueAsString(umidTokenMap));

            String jsonText = objectMapper.writeValueAsString(jsonParams);

            String subUrl = "com.taobao.mtop.mloginunitservice.autologin";
            String url = "https://acs.m.taobao.com/gw/" + subUrl + "/1.0/";

            XHeader xHeader = new XHeader(taobaoAccountEntity);
            xHeader.setSubUrl(subUrl);
            xHeader.setUrlVer("1.0");
            xHeader.setData(jsonText);
            xHeader.setXsign(signService.xsign(xHeader));

            Map<String, String> postParams = new HashMap<>();
            postParams.put("data", jsonText);

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("MTOPSDK/3.1.1.7 (Android;5.1.1)")
                            .addHeaders(xHeader.getHeaders())
                            .addHeader("Content-Type", "application/x-www-form-urlencoded"),
                    new Request("POST", url, ResponseType.TEXT)
                            .setParameters(postParams),
                    new DefaultCookieStorePool(taobaoAccountEntity.getCookieStore()));
            if (response.getStatusCode() != HttpStatus.SC_OK) {
                return R.error();
            }

            String respText = response.getResult();
            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> map = jsonParser.parseMap(respText);

            TaobaoReturn taobaoReturn = new TaobaoReturn(map);
            if (taobaoReturn.getErrorCode() != ErrorCodes.SUCCESS) {
                taobaoAccountEntity.setState(TaobaoAccountState.AutoLoginFailed.getState());
                taobaoAccountEntity.setUpdatedTime(new Date());
                return R.error(taobaoReturn.getErrorCode(), taobaoReturn.getErrorMsg());
            }

            Map<String, Object> mapData = (Map<String, Object>) map.get("data");
            Map<String, Object> mapReturnValue = (Map<String, Object>) mapData.get("returnValue");
            String data = (String) mapReturnValue.get("data");
            Map<String, Object> mapRetData = jsonParser.parseMap(data);

            String autoLoginToken = String.valueOf(mapRetData.get("autoLoginToken"));
            long expires = Long.parseLong(String.valueOf(mapRetData.get("expires")));
            List<String> lstCookieHeaders = (List<String>) mapRetData.get("cookies");

            List<Cookie> lstCookies = CookieHelper.parseCookieHeaders(url, lstCookieHeaders);

            String sid = String.valueOf(mapRetData.get("sid"));
            String uid = String.valueOf(mapRetData.get("userId"));
            String nick = String.valueOf(mapRetData.get("nick"));

            taobaoAccountEntity.setAutoLoginToken(autoLoginToken);
            taobaoAccountEntity.setSid(sid);
            taobaoAccountEntity.setUid(uid);
            taobaoAccountEntity.setNick(nick);
            taobaoAccountEntity.setExpires(CommonUtils.timestampToDate(expires * 1000));

            CookieStore cookieStore = new BasicCookieStore();
            for (Cookie cookie : lstCookies) {
                cookieStore.addCookie(cookie);
            }
            taobaoAccountEntity.setCookieStore(cookieStore);

            taobaoAccountEntity.setState(TaobaoAccountState.Normal.getState());
            taobaoAccountEntity.setUpdatedTime(new Date());

            return R.ok()
                    .put("taobao_account", taobaoAccountEntity);

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
                return R.error("获取二维码失败");
            }

            String respText = response.getResult();
            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> map = jsonParser.parseMap(respText);

            TaobaoReturn taobaoReturn = new TaobaoReturn(map);
            if (taobaoReturn.getErrorCode() != ErrorCodes.SUCCESS) {
                return R.error(taobaoReturn.getErrorCode(), taobaoReturn.getErrorMsg());
            }

            QRCode qrCode = new QRCode();
            String lgToken = String.valueOf(map.get("at"));
            String qrCodeUrl = String.valueOf(map.get("url"));
            long timestamp = Long.parseLong(String.valueOf(map.get("t")));

            qrCode.setTimestamp(timestamp);
            qrCode.setAccessToken(lgToken);
            qrCode.setNavigateUrl(qrCodeUrl);
            qrCode.setImageUrl("https://gqrcode.alicdn.com/img?type=hv&text=" + URLEncoder.encode(qrCodeUrl) + "&h=160&w=160");

            return R.ok()
                    .put("qrcode", qrCode);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error("获取二维码失败");
    }

    @Override
    public R getLoginQRCodeCsrfToken(TaobaoAccountEntity taobaoAccountEntity, QRCode qrCode) {
        try {
            Map<String, Object> jsonParams = new HashMap<>();
            jsonParams.put("_tbScancodeApproach_", "scan");
            jsonParams.put("ttid", "600000@taobao_android_8.7.1");
            jsonParams.put("shortURL", qrCode.getNavigateUrl());

            String url = "https://login.m.taobao.com/qrcodeLogin.htm?";
            for (String key : jsonParams.keySet()) {
                url += key + "=" + URLEncoder.encode(String.valueOf(jsonParams.get(key))) + "&";
            }

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2)")
                            .setAccept("text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                            .addHeader("Accept-Language", "zh-CN,en-US;q=0.9")
                            .addHeader("x-requested-with", "com.taobao.taobao"),
                    new Request("GET", url, ResponseType.TEXT),
                    new DefaultCookieStorePool(taobaoAccountEntity.getCookieStore()));
            if (response.getStatusCode() != HttpStatus.SC_OK) {
                return R.error("获取二维码失败");
            }

            String respText = response.getResult();

            // 获取csrfToken
            int startPos = respText.indexOf("name=\"csrftoken\"");
            if (startPos < 0)
                return R.error();
            startPos = respText.indexOf("value=\"", startPos);
            if (startPos < 0)
                return R.error();
            startPos += "value=\"".length();

            int endPos = respText.indexOf("\"", startPos);
            if (endPos < 0)
                return R.error();
            String csrfToken = respText.substring(startPos, endPos);

            return R.ok()
                    .put("csrftoken", csrfToken);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error("获取二维码Token失败2");
    }

    @Override
    public R authQRCode(TaobaoAccountEntity taobaoAccountEntity, QRCode qrCode, String csrfToken) {
        try {
            String url = "https://login.m.taobao.com/qrcodeLoginAuthor.do?qr_t=s";

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2)")
                            .setContentType("application/x-www-form-urlencoded"),
                    new Request("POST", url, ResponseType.TEXT)
                            .addParameter("csrftoken", csrfToken)
                            .addParameter("shortURL", qrCode.getNavigateUrl())
                            .addParameter("ql", "true"),
                    new DefaultCookieStorePool(taobaoAccountEntity.getCookieStore()));
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
    public R checkLoginByQRCode(TaobaoAccountEntity taobaoAccountEntity, QRCode qrCode) {
        try {
            XHeader xHeader = new XHeader(new Date());
            ObjectMapper objectMapper = new ObjectMapper();

            Map<String, String> tokenInfo = new HashMap<>();
            tokenInfo.put("utdid", taobaoAccountEntity.getUtdid());
            tokenInfo.put("appName", "25443018");
            tokenInfo.put("token", qrCode.getAccessToken());
            tokenInfo.put("t", String.valueOf(qrCode.getTimestamp()));

            Map<String, String> umidToken = new HashMap<>();
            umidToken.put("umidToken", taobaoAccountEntity.getUmidToken());

            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("tokenInfo", objectMapper.writeValueAsString(tokenInfo));
            paramMap.put("riskControlInfo", objectMapper.writeValueAsString(umidToken));
            paramMap.put("ext", "{}");

            String jsonText = objectMapper.writeValueAsString(paramMap);

            String subUrl = "mtop.taobao.havana.mlogin.qrcodelogin";
            String url = "https://api.m.taobao.com/gw/" + subUrl + "/1.0/?data=" + URLEncoder.encode(jsonText);

            xHeader.setSubUrl(subUrl);
            xHeader.setUrlVer("1.0");
            xHeader.setData(jsonText);
            xHeader.setXsign(signService.xsign(xHeader));

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2)")
                            .setContentType("application/x-www-form-urlencoded")
                            .addHeaders(xHeader.getHeaders()),
                    new Request("GET", url, ResponseType.TEXT));
            if (response.getStatusCode() != HttpStatus.SC_OK) {
                return R.error("验证登录二维码失败");
            }

            String respText = response.getResult();
            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> map = jsonParser.parseMap(respText);

            TaobaoReturn taobaoReturn = new TaobaoReturn(map);
            if (taobaoReturn.getErrorCode() != ErrorCodes.SUCCESS) {
                return R.error(taobaoReturn.getErrorCode(), taobaoReturn.getErrorMsg());
            }

            Map<String, Object> mapData = (Map<String, Object>) map.get("data");
            Map<String, Object> mapReturnValue = (Map<String, Object>) mapData.get("returnValue");
            String data = String.valueOf(mapReturnValue.get("data"));
            Map<String, Object> mapRetData = jsonParser.parseMap(data);

            String autoLoginToken = String.valueOf(mapRetData.get("autoLoginToken"));
            long expires = Long.parseLong(String.valueOf(mapRetData.get("expires")));
            List<String> lstCookieHeaders = (List<String>) mapRetData.get("cookies");

            List<Cookie> lstCookies = CookieHelper.parseCookieHeaders(url, lstCookieHeaders);

            String sid = String.valueOf(mapRetData.get("sid"));
            String uid = String.valueOf(mapRetData.get("userId"));
            String nick = String.valueOf(mapRetData.get("nick"));

            taobaoAccountEntity.setAutoLoginToken(autoLoginToken);
            taobaoAccountEntity.setSid(sid);
            taobaoAccountEntity.setUid(uid);
            taobaoAccountEntity.setNick(nick);
            taobaoAccountEntity.setExpires(CommonUtils.timestampToDate(expires * 1000));

            CookieStore cookieStore = new BasicCookieStore();
            for (Cookie cookie : lstCookies) {
                cookieStore.addCookie(cookie);
            }
            taobaoAccountEntity.setCookieStore(cookieStore);

            taobaoAccountEntity.setState(TaobaoAccountState.Normal.getState());
            taobaoAccountEntity.setUpdatedTime(new Date());

            return R.ok()
                    .put("taobao_account", taobaoAccountEntity);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error("验证登录二维码失败");
    }

    @Override
    public R postpone(TaobaoAccountEntity taobaoAccountEntity) {
        try {
            R r = this.getLoginQRCodeURL();
            if (r.getCode() != ErrorCodes.SUCCESS) {
                return r;
            }
            QRCode qrCode = (QRCode) r.get("qrcode");
            r = this.getLoginQRCodeCsrfToken(taobaoAccountEntity, qrCode);
            String csrfToken = (String) r.get("csrftoken");
            if (r.getCode() != ErrorCodes.SUCCESS || HFStringUtils.isNullOrEmpty(csrfToken)) {
                return r;
            }

            this.checkLoginByQRCode(taobaoAccountEntity, qrCode);
            r = this.authQRCode(taobaoAccountEntity, qrCode, csrfToken);

            boolean success = r.getCode() == ErrorCodes.SUCCESS ? true : false;
            int retry = 0;
            while (success && retry < Config.MAX_RETRY) {
                r = this.checkLoginByQRCode(taobaoAccountEntity, qrCode);
                if (r.getCode() != ErrorCodes.SUCCESS) {
                    retry++;
                    Thread.sleep(100);
                    continue;
                }

                return r;
            }

            taobaoAccountEntity.setState(TaobaoAccountState.Expired.getState());
            taobaoAccountEntity.setUpdatedTime(new Date());

            return r;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R getUmtidToken() {
        try {
            String url = "http://zb.dcdnz.com/api/notify/umtid.php?action=select";
            Response<String> response = HttpHelper.execute(
                new SiteConfig()
                    .setUserAgent("Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2)"),
                new Request("GET", url, ResponseType.TEXT));

            if (response.getStatusCode() != HttpStatus.SC_OK) {
                return R.error("生成UmtidToken失败");
            }

            String respText = response.getResult();
            respText = StringUtils.strip(respText, "\"\r\n");

            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> map = jsonParser.parseMap(respText);

            List<Map> dataList = (List<Map>)map.get("data");
            if (dataList.size() > 0) {
                Map<String, Object> testMap = (Map<String, Object>)dataList.get(0);
                String umtid = String.valueOf(testMap.get("test"));
                return R.ok()
                    .put("umtid", umtid.replace("{", "").replace("}", ""));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error("生成UmtidToken失败");
    }

    @Override
    public R getH5Token(TaobaoAccountEntity taobaoAccountEntity) {
        try {
            String subUrl = "mtop.taobao.baichuan.smb.get";

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("appKey", "21523971");
            paramMap.put("t", "");
            paramMap.put("sign", "");
            paramMap.put("api", subUrl);
            paramMap.put("v", "1.0");
            paramMap.put("type", "originaljson");
            paramMap.put("dataType", "jsonp");
            paramMap.put("timeout", "10000");

            String url = "https://acs.m.taobao.com/h5/" + subUrl + "/1.0/?";
            for (String key : paramMap.keySet()) {
                url += key + "=" + URLEncoder.encode(String.valueOf(paramMap.get(key))) + "&";
            }

            Response<String> response = HttpHelper.execute(
                new SiteConfig()
                    .setUserAgent("Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2)"),
                new Request("GET", url, ResponseType.TEXT),
                new DefaultCookieStorePool(taobaoAccountEntity.getCookieStore()));

            if (response.getStatusCode() != HttpStatus.SC_OK) {
                return R.error();
            }

            List<Cookie> cookies = response.getCookieStore().getCookies();
            taobaoAccountEntity.mergeCookies(cookies);

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R getNewDeviceId(TaobaoAccountEntity taobaoAccountEntity) {
        try {
            String c2 = DeviceUtils.generateIMEI();
            String c3 = DeviceUtils.generatePhoneC3();
            String c6 = CommonUtils.randomAlphabetic("6mqe47k2o48k2b5d".length()).toLowerCase();
            String c4 = DeviceUtils.generateMAC();
            String c5 = CommonUtils.randomAlphabetic("88oikao7".length()).toLowerCase();

            Map<String, Object> jsonParams = new HashMap<>();
            jsonParams.put("c1", "HUAWEI(EVA-AL00)");
            jsonParams.put("c2", c2);
            jsonParams.put("c0", "HUAWEI");
            jsonParams.put("device_global_id", taobaoAccountEntity.getUtdid() + c2 + c3);
            jsonParams.put("c6", c6);
            jsonParams.put("c4", c4);
            jsonParams.put("new_device", "true");
            jsonParams.put("c5", c5);
            jsonParams.put("c3", c3);

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonText = objectMapper.writeValueAsString(jsonParams);

            H5Header h5Header = new H5Header(taobaoAccountEntity, "21523971");
            String subUrl = "mtop.sys.newdeviceid";

            Map<String, Object> urlParams = new HashMap<>();
            urlParams.put("appKey", h5Header.getAppKey());
            urlParams.put("t", String.valueOf(h5Header.getLongTimestamp()));
            urlParams.put("api", subUrl);
            urlParams.put("v", "4.0");
            urlParams.put("data", jsonText);
            urlParams.put("sign", signService.h5sign(h5Header, jsonText));

            String url = "https://h5api.m.taobao.com/h5/" + subUrl + "/4.0/?";

            for (String key : urlParams.keySet()) {
                url += key + "=" + URLEncoder.encode(String.valueOf(urlParams.get(key))) + "&";
            }

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2)"),
                    new Request("GET", url, ResponseType.TEXT),
                    new DefaultCookieStorePool(taobaoAccountEntity.getCookieStore()));

            if (response.getStatusCode() != HttpStatus.SC_OK) {
                return R.error("获取机器码失败");
            }

            String respText = response.getResult();
            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> map = jsonParser.parseMap(respText);

            TaobaoReturn taobaoReturn = new TaobaoReturn(map);
            if (taobaoReturn.getErrorCode() != ErrorCodes.SUCCESS) {
                return R.error(taobaoReturn.getErrorCode(), taobaoReturn.getErrorMsg());
            }

            Map<String, Object> mapData = (Map<String, Object>) map.get("data");
            String deviceId = String.valueOf(mapData.get("device_id"));

            return R.ok()
                    .put("device_id", deviceId);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error("获取机器码失败");
    }
}
