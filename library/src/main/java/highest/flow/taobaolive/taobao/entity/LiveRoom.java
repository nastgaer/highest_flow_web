package highest.flow.taobaolive.taobao.entity;

import highest.flow.taobaolive.taobao.defines.LiveRoomState;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LiveRoom extends PreLiveRoomSpec {

    private String liveId = "";

    private String creatorId = "";

    private String talentLiveUrl = "";

    private String accountId = "";

    private String topic = "";

    private String scopeId = "";

    private String subScopeId = "";

    private String replayUrl = "";

    /**
     * 直播间名称
     */
    private String accountName = "";

    /**
     * 粉丝数
     */
    private int fansNum = 0;

    /**
     * 观看次数
     */
    private int viewCount = 0;

    /**
     * 观看人数
     */
    private int personCount = 0;

    /**
     * 在线人数
     */
    private int onlineCount = 0;

    /**
     * 点赞数
     */
    private int praiseCount = 0;

    /**
     * 互动信息数
     */
    private int messageCount = 0;

    /**
     * 有没有排位赛
     */
    private boolean hasRankingEntry = false;

    /**
     * 当前的热度值
     */
    private int rankingScore = 0;

    /**
     * 当前的排位
     */
    private int rankingNum = 0;

    /**
     * 排位赛赛道
     */
    private String rankingName = "";

    private List<Product> products = new ArrayList<>();
}
