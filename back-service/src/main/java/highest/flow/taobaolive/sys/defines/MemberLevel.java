package highest.flow.taobaolive.sys.defines;

public enum MemberLevel {

    Normal(0),
    TestUser(5),
    LicenseUser(10),
    Administrator(99);

    private int level = 0;

    MemberLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}
