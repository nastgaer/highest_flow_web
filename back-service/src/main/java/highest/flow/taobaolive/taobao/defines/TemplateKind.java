package highest.flow.taobaolive.taobao.defines;

public enum TemplateKind {

    WithPlaying(0), // 灵魂模板
    NonPlaying(1); // 盲眼模板

    private int kind = 0;

    TemplateKind(int kind) {
        this.kind = kind;
    }

    public int getKind() {
        return this.kind;
    }
}
