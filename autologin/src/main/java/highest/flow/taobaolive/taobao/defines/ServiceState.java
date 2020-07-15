package highest.flow.taobaolive.taobao.defines;

public enum ServiceState {

    Waiting(0), // 等待
    Normal(1), // 正常
    Suspended(2), // 暂停
    Stopped(3); // 停止

    private int state = 0;

    ServiceState(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }
}
