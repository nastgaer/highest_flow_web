package highest.flow.taobaolive.taobao.defines;

public enum ProductState {

    Active(0), // 正常
    Expired(1); // 已经下架了

    private int state = 0;

    ProductState(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }
}
