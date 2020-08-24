package highest.flow.taobaolive.taobao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@TableName("tbl_liveroom_history")
public class LiveRoomEntity extends BaseLiveRoom {

    @TableId(type = IdType.AUTO)
    private int id;

    private String taobaoAccountNick;

    /**
     * LiveRoomKind
     */
    private int liveKind;

    private String liveId;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date liveStartedTime;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date liveEndTime;

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
    @JsonIgnore
    private String creatorId = "";

    @TableField(exist = false)
    @JsonIgnore
    private String talentLiveUrl = "";

    @TableField(exist = false)
    private String accountId = "";

    @TableField(exist = false)
    @JsonIgnore
    private String topic = "";

    @Data
    public class HierarchyData {

        private String scopeId = "";

        private String subScopeId = "";
    }

    @TableField(exist = false)
    private HierarchyData hierarchyData = new HierarchyData();

    @TableField(exist = false)
    @JsonIgnore
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
     * 有没有总榜
     */
    @TableField(exist = false)
    private boolean hasRankingListEntry = false;

    @Data
    public class RankingListDataBiz {

        /**
         * 当前的助力值
         */
        private int rankingScore = 0;

        /**
         * 当前的排位
         */
        private int rankingNum = 0;

        /**
         * 排位赛赛道
         */
        private String rankingName = "";
    }

    @TableField(exist = false)
    private RankingListDataBiz rankingListData = new RankingListDataBiz();

    /**
     * 有没有小时榜
     */
    @TableField(exist = false)
    private boolean hasHourRankingListEntry = false;

    @Data
    public class HourRankingListDataBiz {

        /**
         * 当前的助力值
         */
        private int rankingScore = 0;

        /**
         * 当前的排位
         */
        private int rankingNum = 0;

        /**
         * 排位赛赛道
         */
        private String rankingName = "";
    }

    @TableField(exist = false)
    private HourRankingListDataBiz hourRankingListData = new HourRankingListDataBiz();

    /**
     * 采集的商品
     */
    @TableField(exist = false)
    private List<ProductEntity> reservedProducts = new ArrayList<>();

    /**
     * 上架的商品
     */
    @TableField(exist = false)
    private List<ProductEntity> products = new ArrayList<>();
}
