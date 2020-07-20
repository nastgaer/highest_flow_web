package highest.flow.taobaolive.api.param;

import highest.flow.taobaolive.taobao.entity.LiveRoomStrategyEntity;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class SetLiveRoomStrategyParam {

    private String taobaoAccountNick;

    private Date operationStartTime;

    private List<LiveRoomStrategyEntity> liveRoomStrategies = new ArrayList<>();
}
