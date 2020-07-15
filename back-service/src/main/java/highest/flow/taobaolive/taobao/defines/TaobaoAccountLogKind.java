package highest.flow.taobaolive.taobao.defines;

public enum TaobaoAccountLogKind {

    AutoLogin(0),
    Postpone(1),
    New(2),
    Update(3),
    Delete(3);

    private int kind = 0;

    TaobaoAccountLogKind(int kind) {
        this.kind = kind;
    }

    public int getKind() {
        return kind;
    }
}
