package highest.flow.taobaolive.taobao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@TableName("tbl_member_tcc")
public class MemberTaoAccEntity {

    @TableId(type = IdType.AUTO)
    private int id;

    private int memberId;

    @TableId
    private String taobaoAccountNick;

    private String roomName;

    private String comment;

    /**
     * ServiceState
     */
    private int state;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date serviceStartDate;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date serviceEndDate;

    @JsonFormat(timezone = "GMT+8", pattern = "HH:mm:ss")
    @DateTimeFormat(pattern = "HH:mm:ss")
    private Date operationStartTime;

    @TableField(exist = false)
    private List<LiveRoomStrategyEntity> liveRoomStrategies = new ArrayList<>();

    @TableField(exist = false)
    private List<PreLiveRoomSpecEntity> preLiveRoomSpecs = new ArrayList<>();

    private Date createdTime;

    private Date updatedTime;
}
