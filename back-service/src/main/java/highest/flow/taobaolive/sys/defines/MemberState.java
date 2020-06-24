package highest.flow.taobaolive.sys.defines;

public enum MemberState {

    Normal(0),
    Deleted(1),
    Suspended(2);

    private int state = 0;

    MemberState(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }
}
