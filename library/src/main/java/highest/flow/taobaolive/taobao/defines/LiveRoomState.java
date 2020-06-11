package highest.flow.taobaolive.taobao.defines;

public enum LiveRoomState {

    Preparing(0), // 默认状态， 等待发布
    Published(1), // 成功发布预告
    Started(2), // 已经开始
    Pushing(4), // 正在推流
    Stopped(5), // 回放
    Deleted(6); // 删除

    private int state = 0;

    LiveRoomState(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }
}
