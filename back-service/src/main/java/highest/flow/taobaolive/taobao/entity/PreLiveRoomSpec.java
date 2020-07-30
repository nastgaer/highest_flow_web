package highest.flow.taobaolive.taobao.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class PreLiveRoomSpec {

    private String liveCoverImg;

    private String liveCoverImg169;

    private String liveTitle;

    private String liveIntro;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date liveAppointmentTime;

    private int liveChannelId;

    private int liveColumnId;

    private String liveLocation;

    private String hotProductUrl;
}
