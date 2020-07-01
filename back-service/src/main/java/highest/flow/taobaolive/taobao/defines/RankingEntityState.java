package highest.flow.taobaolive.taobao.defines;

public enum RankingEntityState {

    Waiting(0), // 未开始
    Running(1), // 执行中
    Finished(2), // 结束
    Error(3);   // 错误

    private int state = 0;

    RankingEntityState(int state) {
        this.state = state;
    }

    public int getState() {
        return this.state;
    }
}
