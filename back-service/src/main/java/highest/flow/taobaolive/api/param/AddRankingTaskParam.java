package highest.flow.taobaolive.api.param;

import com.fasterxml.jackson.annotation.JsonFormat;
import highest.flow.taobaolive.taobao.entity.PreLiveRoomSpecEntity;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class AddRankingTaskParam {

    /**
     * 淘口令
     */
    private String taocode;

    /**
     * 直播间ID
     */
    private String liveId;

    /**
     * 直播间名称
     */
    private String accountName;

    /**
     * 目标助力值
     */
    private int targetScore;

    /**
     * 是否包含关注
     */
    private boolean hasFollow;

    /**
     * 是否包含停留
     */
    private boolean hasStay;

    /**
     * 是否包含购买
     */
    private boolean hasBuy;

    /**
     * 是不是加购
     */
    private boolean hasDoubleBuy;

    /**
     * 预约时间，null 或者 现在的时间表示立即开始
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    /**
     * 备注
     */
    private String comment;
}
