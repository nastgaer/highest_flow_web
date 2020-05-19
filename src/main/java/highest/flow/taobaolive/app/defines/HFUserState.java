package highest.flow.taobaolive.app.defines;

public enum HFUserState {

    Normal(0),
    Deleted(1),
    Suspended(2);

    private int state = 0;
    HFUserState(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }
}
