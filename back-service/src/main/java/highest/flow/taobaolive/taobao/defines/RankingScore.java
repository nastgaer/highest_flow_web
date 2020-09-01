package highest.flow.taobaolive.taobao.defines;

public enum RankingScore {

    Follow("RANK_CONF_FOLLOW_SCORE"),
    Stay("RANK_CONF_STAY_SCORE"),
    Buy("RANK_CONF_BUY_SCORE"),
    DoubleBuy("RANK_CONF_DOUBLEBUY_SCORE");

    private String confKey = "";

    RankingScore(String confKey) {
        this.confKey = confKey;
    }

    public String getConfKey() {
        return this.confKey;
    }
}
