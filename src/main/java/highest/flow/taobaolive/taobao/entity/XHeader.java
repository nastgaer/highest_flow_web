package highest.flow.taobaolive.taobao.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import highest.flow.taobaolive.common.utils.HFStringUtils;
import lombok.Data;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Data
public class XHeader {

    private String utdid;
    private String uid;
    private String appkey = "21646297";
    private String pv = "5.1";
    private long timestamp = 0;
    private String sid;
    private String ttid = "600000@taobao_android_7.6.0"; //"600000%40taobao%5Fandroid%5F7.6.0";
    private String devid;
    private String location1 = "1568.459875";
    private String location2 = "454.451236";
    private String features = "27";
    private String subUrl;
    private String urlVer;
    private String data;
    @JsonIgnore
    private String xsign;

    public XHeader() {}

    public XHeader(Date date) {
        this.timestamp = date.getTime();
    }

    public XHeader(TaobaoAccount taobaoAccount) {
        this.utdid = taobaoAccount.getUtdid();
        this.devid = taobaoAccount.getDevid();
        this.sid = taobaoAccount.getSid();
        this.uid = taobaoAccount.getAccountId();

        this.timestamp = new Date().getTime();
    }

    @JsonIgnore
    public long getLongTimestamp() {
        return timestamp;
    }

    @JsonIgnore
    public long getShortTimestamp() {
        return timestamp / 1000;
    }

    @JsonIgnore
    public String getLocation() {
        return location2 + "&" + location1; // "454.451236%2C1568.459875";
    }

    @JsonIgnore
    public Map<String, String> getHeaders() {
        Map<String, String> map = new HashMap<>();
        map.put("x-appkey", appkey);
        map.put("x-t", String.valueOf(getShortTimestamp()));
        map.put("x-pv", pv);
        map.put("x-sign", xsign);
        map.put("x-features", String.valueOf(features));
        map.put("x-location", getLocation());
        map.put("x-ttid", ttid);
        if (HFStringUtils.isNullOrEmpty(utdid)) {
            map.put("x-utdid", utdid);
        }
        if (HFStringUtils.isNullOrEmpty(devid)) {
            map.put("x-devid", devid);
        }
        if (HFStringUtils.isNullOrEmpty(uid)) {
            map.put("x-uid", uid);
        }
        if (HFStringUtils.isNullOrEmpty(uid)) {
            map.put("x-sid", sid);
        }
        return map;
    }
}
