package highest.flow.taobaolive.taobao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("tbl_ranking_task")
public class RankingEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    private int id;

    private int memberId;

    private String taocode;

    private String liveId;

    private String accountId;

    /**
     * 直播间名称
     */
    private String roomName;

    /**
     * 初始助力值
     */
    private int startScore;

    /**
     * 最后助力值
     */
    private int endScore;

    /**
     * 目标助力值
     */
    private int targetScore;

    /**
     * 是否包含关注、停留、购买、加购
     */
    private boolean hasFollow;
    private boolean hasStay;
    private boolean hasBuy;
    private boolean hasDoubleBuy;

    /**
     * 开始时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    /**
     * 结束时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    private String comment;

    /**
     * RankingEntityState, 任务执行状态
     */
    private int state;

    private String msg;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdTime;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updatedTime;

}
