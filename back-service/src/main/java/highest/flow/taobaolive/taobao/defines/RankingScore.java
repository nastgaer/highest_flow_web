package highest.flow.taobaolive.taobao.defines;

public enum RankingScore {

    DoubleBuyFollow(2000),
    DoubleBuyBuy(20),
    DoubleBuyWatch(30),

    Follow(20),
    Watch(30),
    Buy(0);

    private int score = 0;

    RankingScore(int score) {
        this.score = score;
    }

    public int getScore() {
        return this.score;
    }
}
