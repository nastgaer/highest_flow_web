package highest.flow.taobaolive.security.defines;

public enum LicenseCodeType {

    Test(0),        // 测试
    License(1);     // 授权

    private int type = 0;

    LicenseCodeType(int type) {
        this.type = type;
    }

    public int getType() {
        return this.type;
    }

    public static LicenseCodeType from(int type) {
        if (License.getType() == type) {
            return License;
        }
        return Test;
    }
}
