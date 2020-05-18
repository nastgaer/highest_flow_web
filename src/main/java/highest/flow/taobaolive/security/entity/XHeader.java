package highest.flow.taobaolive.security.entity;

import lombok.Data;

@Data
public class XHeader {

    private String utdid;
    private String uid;
    private String appkey;
    private String aes;
    private String timestamp;
    private String url;
    private String urlVer;
    private String sid;
    private String ttid;
    private String devid;
    private String location;
    private String features;
}
