package highest.taolive.xdata.entity;

import java.util.Map;

public class UnifiedSignParam {

    private Map<String,String> p1;

    private Map<String,String> p2;

    private String p3;

    private String p4;

    private boolean p5;

    private String version = "6.3";

    public Map<String, String> getP1() {
        return p1;
    }

    public void setP1(Map<String, String> p1) {
        this.p1 = p1;
    }

    public Map<String, String> getP2() {
        return p2;
    }

    public void setP2(Map<String, String> p2) {
        this.p2 = p2;
    }

    public String getP3() {
        return p3;
    }

    public void setP3(String p3) {
        this.p3 = p3;
    }

    public String getP4() {
        return p4;
    }

    public void setP4(String p4) {
        this.p4 = p4;
    }

    public boolean isP5() {
        return p5;
    }

    public void setP5(boolean p5) {
        this.p5 = p5;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
