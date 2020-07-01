package highest.flow.taobaolive.taobao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@TableName("tbl_liveroom_history")
public class LiveRoomEntity extends LiveRoom {

    @TableId(type = IdType.AUTO)
    private int id;

    private String taobaoAccountId;

    /**
     * LiveRoomKind
     */
    private int liveKind;

    private String liveId;

    /**
     * 直播间状态 LiveRoomState
     */
    private int liveState;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdTime;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updatedTime;

    @TableField(exist = false)
    private String creatorId = "";

    @TableField(exist = false)
    private String talentLiveUrl = "";

    @TableField(exist = false)
    private String accountId = "";

    @TableField(exist = false)
    private String topic = "";

    @TableField(exist = false)
    private String scopeId = "";

    @TableField(exist = false)
    private String subScopeId = "";

    @TableField(exist = false)
    /**
     *
     */
    private String replayUrl = "";

    /**
     * 直播间名称
     */
    @TableField(value = "live_room_name")
    private String accountName = "";

    /**
     * 粉丝数
     */
    @TableField(exist = false)
    private int fansNum = 0;

    /**
     * 观看次数
     */
    @TableField(exist = false)
    private int viewCount = 0;

    /**
     * 观看人数
     */
    @TableField(exist = false)
    private int personCount = 0;

    /**
     * 在线人数
     */
    @TableField(exist = false)
    private int onlineCount = 0;

    /**
     * 点赞数
     */
    @TableField(exist = false)
    private int praiseCount = 0;

    /**
     * 互动信息数
     */
    @TableField(exist = false)
    private int messageCount = 0;

    /**
     * 有没有排位赛
     */
    @TableField(exist = false)
    private boolean hasRankingEntry = false;

    /**
     * 当前的热度值
     */
    @TableField(exist = false)
    private int rankingScore = 0;

    /**
     * 当前的排位
     */
    @TableField(exist = false)
    private int rankingNum = 0;

    /**
     * 排位赛赛道
     */
    @TableField(exist = false)
    private String rankingName = "";

    /**
     * 采集的商品
     */
    @TableField(exist = false)
    private List<ProductEntity> reservedProductEntities = new ArrayList<>();

    /**
     * 上架的商品
     */
    @TableField(exist = false)
    private List<ProductEntity> productEntities = new ArrayList<>();
}
