package highest.flow.taobaolive.api.param;

import com.fasterxml.jackson.annotation.JsonFormat;
import highest.flow.taobaolive.taobao.entity.PreLiveRoomSpecEntity;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class AddRankingTaskParam {

    private String taocode;

    private int targetScore;

    private boolean doubleBuy;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;
}
