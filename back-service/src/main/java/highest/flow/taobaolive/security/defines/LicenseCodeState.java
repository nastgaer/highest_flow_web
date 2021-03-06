package highest.flow.taobaolive.security.defines;

public enum LicenseCodeState {

    Created(0),
    Accepted(1), // 验证，绑定机器
    Deleted(2);

    private int state = 0;

    LicenseCodeState(int state) {
        this.state = state;
    }

    public int getState() {
        return this.state;
    }
}
