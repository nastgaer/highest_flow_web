package highest.flow.taobaolive.api.param;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@Data
public class AddRankingTaskParam2 {

    @JsonProperty("taocode")
    private String taocode;

    @JsonProperty("live_id")
    private String liveId;

    @JsonProperty("room_name")
    private String roomName;

    @JsonProperty("target_score")
    private int targetScore;

    @JsonProperty("has_follow")
    private boolean hasFollow;

    @JsonProperty("has_stay")
    private boolean hasStay;

    @JsonProperty("has_buy")
    private boolean hasBuy;

    @JsonProperty("has_double_buy")
    private boolean hasDoubleBuy;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("start_time")
    private Date startTime;

    @JsonProperty("comment")
    private String comment;
}
