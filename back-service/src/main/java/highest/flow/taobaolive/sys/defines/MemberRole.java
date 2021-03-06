package highest.flow.taobaolive.sys.defines;

public enum MemberRole {

    members(0),              // 会员管理
    tb_accounts(1),       // 小号管理
    ranking(2),             // 刷热度
    tblive(3);          // 淘宝直播

    private int role = 0;

    MemberRole(int role) {
        this.role = role;
    }

    public int getRole() {
        return this.role;
    }
}
