package highest.flow.taobaolive.security.defines;

import highest.flow.taobaolive.security.service.LicenseService;

public enum LicenseCodeState {

    Created(0),
    Accepted(1), // 验证，绑定机器
    Binded(2); // 验证，绑定淘宝账号

    private int state = 0;

    LicenseCodeState(int state) {
        this.state = state;
    }

    public int getState() {
        return this.state;
    }
}
