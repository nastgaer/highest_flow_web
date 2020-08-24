package highest.flow.taobaolive.sys.defines;

public enum MemberLevel {

    Normal(0), // 公司员工
    TestUser(5), // 测试用户
    LicenseUser(10), // 授权用户
    Administrator(99); // 管理员

    private int level = 0;

    MemberLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}
