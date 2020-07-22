package highest.flow.taobaolive.sys.defines;

public enum MemberServiceType {

    Ranking(0),     // 刷热度
    TaobaoLive(1);  // 淘宝直播

    private int serviceType = 0;

    MemberServiceType(int serviceType) {
        this.serviceType = serviceType;
    }

    public int getServiceType() {
        return this.serviceType;
    }

    public static MemberServiceType from(int type) {
        if (TaobaoLive.getServiceType() == type) {
            return TaobaoLive;
        }
        return Ranking;
    }
}
