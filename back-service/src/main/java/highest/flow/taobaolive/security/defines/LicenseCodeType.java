package highest.flow.taobaolive.security.defines;

public enum LicenseCodeType {

    Internal(0),    // 内部
    Test(1),        // 测试
    License(2);     // 授权

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
        if (Internal.getType() == type) {
            return Internal;
        }
        return Test;
    }
}
