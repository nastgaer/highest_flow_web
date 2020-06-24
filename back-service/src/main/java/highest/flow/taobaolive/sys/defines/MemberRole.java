package highest.flow.taobaolive.sys.defines;

public enum MemberRole {

    Member(0),              // 会员管理
    TaobaoAccount(1),       // 小号管理
    Ranking(2),             // 刷热度
    TaobaoLive(3);          // 淘宝直播

    private int role = 0;

    MemberRole(int role) {
        this.role = role;
    }

    public int getRole() {
        return this.role;
    }
}
