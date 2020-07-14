package highest.flow.taobaolive.api.param;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import highest.flow.taobaolive.taobao.entity.PreLiveRoomSpecEntity;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class AddRoomParam {

    private String taobaoAccountNick;

    private String comment;

    @Data
    public class Service {

        @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
        private Date startDate;

        private int days;

    }

    private Service service;

    private List<PreLiveRoomSpecEntity> liveSpecs;
}
