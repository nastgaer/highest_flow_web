package highest.flow.taobaolive.taobao.defines;

public enum TaobaoAccountState {

    Normal(0),
    Expired(1),
    AutoLoginFailed(2);

    private int state = 0;

    TaobaoAccountState(int state) {
        this.state = state;
    }

    public int getState() {
        return this.state;
    }

    public static TaobaoAccountState fromInt(int state) {
        if (state == Normal.getState()) {
            return Normal;
        } else if (state == Expired.getState()) {
            return Expired;
        } else if (state == AutoLoginFailed.getState()) {
            return AutoLoginFailed;
        }
        return Normal;
    }
}
