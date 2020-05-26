package highest.flow.taobaolive.app.defines;

public enum HFUserLevel {

    Guest(0),
    User(1),
    Adminitrator(99);

    private int level = 0;
    HFUserLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}
