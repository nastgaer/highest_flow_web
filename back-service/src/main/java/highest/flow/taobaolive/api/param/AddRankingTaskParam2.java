package highest.flow.taobaolive.api.param;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@Data
public class AddRankingTaskParam2 {

    @Data
    public class LiveRoom {

        @JsonProperty("live_id")
        private String liveId;

        @JsonProperty("account_id")
        private String accountId;

        @JsonProperty("scope_id")
        private String scopeId;

        @JsonProperty("sub_scope_id")
        private String subScopeId;

        @JsonProperty("room_name")
        private String roomName;
    }

    @JsonProperty("live_room")
    private LiveRoom liveRoom;

    @JsonProperty("target_score")
    private int targetScore;

    @JsonProperty("double_buy")
    private boolean doubleBuy;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("start_time")
    private Date startTime;
}
