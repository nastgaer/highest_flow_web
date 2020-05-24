package highest.flow.taobaolive.taobao.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import highest.flow.taobaolive.common.utils.HFStringUtils;
import lombok.Data;

import java.net.URLEncoder;
import java.util.Calendar;
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
    private String ttid = "600000@taobao_android_7.6.0";
    private String devid;
    private String location1 = "1568.459875";
    private String location2 = "454.451236";
    private String features = "27";
    private String subUrl;
    private String urlVer;
    private String data;
    @JsonIgnore
    private String xsign;
    @JsonIgnore
    private boolean encoded = false;

    public XHeader() {}

    public XHeader(Date date)  {
        this();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        this.timestamp = calendar.getTimeInMillis();
    }

    public XHeader(TaobaoAccount taobaoAccount) {
        this(new Date());
        this.utdid = taobaoAccount.getUtdid();
        this.devid = taobaoAccount.getDevid();
        this.sid = taobaoAccount.getSid();
        this.uid = taobaoAccount.getAccountId();
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
        if (encoded) {
            return URLEncoder.encode(ttid).replace("_", "%5F");
        }
        return ttid;
    }

    @JsonIgnore
    public String getLocation() {
        if (encoded)
            return URLEncoder.encode(location1 + "," + location2);
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
