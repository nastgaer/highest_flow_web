package highest.flow.taobaolive.api.param;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class TodayRankingParam {

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    private Date currentDate;
}
