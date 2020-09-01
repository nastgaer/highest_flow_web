package highest.flow.taobaolive.taobao.defines;

public enum RankingEntityState {

    Waiting(0), // 未开始
    Running(1), // 执行中
    Stopped(2), // 停止
    Finished(3), // 结束
    Error(4),   // 错误
    Deleted(5); // 已删除

    private int state = 0;

    RankingEntityState(int state) {
        this.state = state;
    }

    public int getState() {
        return this.state;
    }
}
