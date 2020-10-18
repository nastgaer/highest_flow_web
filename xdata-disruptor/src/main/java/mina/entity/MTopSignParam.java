package mina.entity;

import java.util.Map;

public class MTopSignParam {

    private Map<String,String> p1;

    private String p2;

    private String p3 = null;

    private String version = "6.2";

    public Map<String, String> getP1() {
        return p1;
    }

    public void setP1(Map<String, String> p1) {
        this.p1 = p1;
    }

    public String getP2() {
        return p2;
    }

    public void setP2(String p2) {
        this.p2 = p2;
    }

    public String getP3() {
        return p3;
    }

    public void setP3(String p3) {
        this.p3 = p3;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
