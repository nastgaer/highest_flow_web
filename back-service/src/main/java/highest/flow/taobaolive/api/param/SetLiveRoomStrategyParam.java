package highest.flow.taobaolive.api.param;

import com.fasterxml.jackson.annotation.JsonFormat;
import highest.flow.taobaolive.taobao.entity.LiveRoomStrategyEntity;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class SetLiveRoomStrategyParam {

    private String taobaoAccountNick;

    @JsonFormat(timezone = "GMT+8", pattern = "HH:mm:ss")
    @DateTimeFormat(pattern = "HH:mm:ss")
    private Date operationStartTime;

    private List<LiveRoomStrategyEntity> liveRoomStrategies = new ArrayList<>();
}
