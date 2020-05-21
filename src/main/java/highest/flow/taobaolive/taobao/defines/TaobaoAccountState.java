package highest.flow.taobaolive.taobao.defines;

public enum TaobaoAccountState {

    Normal(0),
    Expired(1);

    private int state = 0;
    TaobaoAccountState(int state) {
        this.state = state;
    }

    public int getState() {
        return this.state;
    }
}
