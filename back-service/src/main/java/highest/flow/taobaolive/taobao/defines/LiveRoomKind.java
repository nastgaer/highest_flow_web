package highest.flow.taobaolive.taobao.defines;

public enum LiveRoomKind {

    Real(0), // 正式预告
    Flow(1); // 高级引流预告

    private int kind = 0;

    LiveRoomKind(int kind) {
        this.kind = kind;
    }

    public int getKind() {
        return this.kind;
    }
}
