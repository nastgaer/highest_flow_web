package highest.flow.taobaolive.taobao.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import highest.flow.taobaolive.common.utils.CommonUtils;
import highest.flow.taobaolive.common.utils.HFStringUtils;
import lombok.Data;

import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Data
public class XHeader {

    private String utdid = "";
    private String uid = "";
    private String appkey = "25443018";
    private String pv = "6.2";
    private long timestamp = 0;
    private String sid = "";
    private String ttid = "10005533@taobaolive_android_1.4.0";
    private String devid = "";
    private String location1 = "1568.459875";
    private String location2 = "454.451236";
    private String features = "27";
    private String subUrl = "";
    private String urlVer = "";
    private String data = "";
    @JsonIgnore
    private String xsign = "";

    public XHeader() {
    }

    public XHeader(Date date) {
        this();

        this.timestamp = CommonUtils.dateToTimestamp(date);
    }

    public XHeader(TaobaoAccountEntity taobaoAccountEntity) {
        this(new Date());
        this.utdid = taobaoAccountEntity.getUtdid();
        this.devid = taobaoAccountEntity.getDevid();
        this.sid = taobaoAccountEntity.getSid();
        this.uid = taobaoAccountEntity.getUid();
    }

    @JsonIgnore
    public long getLongTimestamp() {
        return timestamp;
    }

    @JsonIgnore
    public long getShortTimestamp() {
        return timestamp / 1000;
    }

    public String getTtid() {
        return ttid;
    }

    @JsonIgnore
    public String getLocation() {
        return location1 + "," + location2;
    }

    @JsonIgnore
    public Map<String, String> getHeaders() {
        Map<String, String> map = new HashMap<>();
        map.put("x-appkey", appkey);
        map.put("x-t", String.valueOf(getShortTimestamp()));
        map.put("x-pv", pv);
        map.put("x-sign", xsign);
        map.put("x-features", String.valueOf(features));
//        map.put("x-location", getLocation());
        map.put("x-ttid", getTtid());
        if (!HFStringUtils.isNullOrEmpty(utdid)) {
            map.put("x-utdid", utdid);
        }
        if (!HFStringUtils.isNullOrEmpty(devid)) {
            map.put("x-devid", devid);
        }
        if (!HFStringUtils.isNullOrEmpty(uid)) {
            map.put("x-uid", uid);
        }
        if (!HFStringUtils.isNullOrEmpty(uid)) {
            map.put("x-sid", sid);
        }
        return map;
    }
}
