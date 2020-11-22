package highest.flow.taolive.xdata;

import org.apache.commons.lang3.StringUtils;

import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class XHeader {

    private String utdid = "";
    private String uid = "";
    private String appkey = "25443018";
    private String pv = "6.3"; // "6.2";
    private long timestamp = 0;
    private String sid = "";
    private String ttid = "10005533@taobaolive_android_1.8.4"; // "10005533@taobaolive_android_1.4.0";
    private String devid = "";
    private String location1 = "1568.459875";
    private String location2 = "454.451236";
    private String features = "27";
    private String subUrl = "";
    private String urlVer = "";
    private String data = "";
    
    private String xSign = "";
    
    private String xSgext = "";
    
    private String xUmt = "";
    
    private String xMiniWua = "";

    public XHeader() {
    }

    public String getUtdid() {
        return utdid;
    }

    public void setUtdid(String utdid) {
        this.utdid = utdid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    public String getPv() {
        return pv;
    }

    public void setPv(String pv) {
        this.pv = pv;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public void setTtid(String ttid) {
        this.ttid = ttid;
    }

    public String getDevid() {
        return devid;
    }

    public void setDevid(String devid) {
        this.devid = devid;
    }

    public String getLocation1() {
        return location1;
    }

    public void setLocation1(String location1) {
        this.location1 = location1;
    }

    public String getLocation2() {
        return location2;
    }

    public void setLocation2(String location2) {
        this.location2 = location2;
    }

    public String getFeatures() {
        return features;
    }

    public void setFeatures(String features) {
        this.features = features;
    }

    public String getSubUrl() {
        return subUrl;
    }

    public void setSubUrl(String subUrl) {
        this.subUrl = subUrl;
    }

    public String getUrlVer() {
        return urlVer;
    }

    public void setUrlVer(String urlVer) {
        this.urlVer = urlVer;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getxSign() {
        return xSign;
    }

    public void setxSign(String xSign) {
        this.xSign = xSign;
    }

    public String getxSgext() {
        return xSgext;
    }

    public void setxSgext(String xSgext) {
        this.xSgext = xSgext;
    }

    public String getxUmt() {
        return xUmt;
    }

    public void setxUmt(String xUmt) {
        this.xUmt = xUmt;
    }

    public String getxMiniWua() {
        return xMiniWua;
    }

    public void setxMiniWua(String xMiniWua) {
        this.xMiniWua = xMiniWua;
    }

    public long getLongTimestamp() {
        return timestamp;
    }

    public long getShortTimestamp() {
        return timestamp / 1000;
    }

    public String getTtid() {
        return ttid;
    }

    public String getLocation() {
        return location1 + "," + location2;
    }

    public Map<String, String> getHeaders() {
        Map<String, String> map = new HashMap<>();
        map.put("x-appkey", appkey);
        map.put("x-t", String.valueOf(getShortTimestamp()));
        map.put("x-pv", pv);
        map.put("x-sign", URLEncoder.encode(xSign));
        map.put("x-features", String.valueOf(features));
//        map.put("x-location", getLocation());
        map.put("x-ttid", URLEncoder.encode(getTtid()));
//        map.put("x-ttid", getTtid());
        if (StringUtils.isNotBlank(utdid)) {
            map.put("x-utdid", URLEncoder.encode(utdid));
//            map.put("x-utdid", utdid);
        }
        if (StringUtils.isNotBlank(devid)) {
            map.put("x-devid", devid);
        }
        if (StringUtils.isNotBlank(uid)) {
            map.put("x-uid", uid);
        }
        if (StringUtils.isNotBlank(uid)) {
            map.put("x-sid", sid);
        }
        if (StringUtils.isNotBlank(xSgext)) {
            map.put("x-sgext", URLEncoder.encode(xSgext));
//            map.put("x-sgext", xSgext);
        }
//        if (StringUtils.isNotBlank(xUmt)) {
//            map.put("x-umt", URLEncoder.encode(xUmt));
//        }
        if (StringUtils.isNotBlank(xMiniWua)) {
            map.put("x-mini-wua", URLEncoder.encode(xMiniWua));
//            map.put("x-mini-wua", xMiniWua);
        }
        return map;
    }
}
