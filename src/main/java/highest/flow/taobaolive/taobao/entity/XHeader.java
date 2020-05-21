package highest.flow.taobaolive.taobao.entity;

import highest.flow.taobaolive.common.utils.HFStringUtils;
import highest.flow.taobaolive.taobao.service.XSignService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.DateTimeException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Data
public class XHeader {

    @Autowired
    private XSignService xSignService;

    private String utdid;
    private String uid;
    private String appkey = "21646297";
    private String pv = "5.1";
    private String aes;
    private Date timestamp = new Date();
    private String url;
    private String urlVer;
    private String sid;
    private String ttid = "600000%40taobao%5Fandroid%5F7.6.0";
    private String devid;
    private String location = "1568.459875%2C454.451236";
    private String features = "27";
    private String xsign;

    public XHeader(Date date) {
        this.timestamp = date;

        this.setXsign(xSignService.sign(this));
    }

    public XHeader(TaobaoAccount taobaoAccount) {
        this.utdid = taobaoAccount.getUtdid();
        this.devid = taobaoAccount.getDevid();
        this.sid = taobaoAccount.getSid();
        this.uid = taobaoAccount.getAccountId();

        timestamp = new Date();

        this.setXsign(xSignService.sign(this));
    }

    public long getLongTimestamp() {
        return timestamp.getTime();
    }

    public long getShortTimestamp() {
        return timestamp.getTime() / 1000;
    }

    public Map<String, String> getHeaders() {
        Map<String, String> map = new HashMap<>();
        map.put("x-appkey", appkey);
        map.put("x-t", String.valueOf(getShortTimestamp()));
        map.put("x-pv", pv);
        map.put("x-sign", xsign);
        map.put("x-features", String.valueOf(features));
        map.put("x-location", location);
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
