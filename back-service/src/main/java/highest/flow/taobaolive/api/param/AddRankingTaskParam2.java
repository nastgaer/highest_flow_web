package highest.flow.taobaolive.api.param;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class AddRankingTaskParam2 {

    @Data
    public class LiveRoomInfo {

        private String liveId;

        private String accountId;

        private String scopeId;

        private String subScopeId;

        private String roomName;
    }

    private LiveRoomInfo liveRoom;

    private int targetScore;

    private boolean doubleBuy;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;
}
