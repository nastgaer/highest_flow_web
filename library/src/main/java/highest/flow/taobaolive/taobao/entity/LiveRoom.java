package highest.flow.taobaolive.taobao.entity;

import highest.flow.taobaolive.taobao.defines.LiveRoomState;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LiveRoom extends PreLiveRoomSpec {

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

    private List<Product> productList = new ArrayList<>();
}
