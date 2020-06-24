package highest.flow.taobaolive.taobao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("tbl_rooms")
public class PreLiveRoomSpec {

    @TableId
    private int id;

    private String username;

    private String taobaoAccountId;

    private String roomName;

    private String coverImg;

    private String coverImg169;

    private String title;

    private String intro;

    private Date startTime;

    private Date endTime;

    private int channelId;

    private int columnId;

    private String location;

    private String hotProductUrl;

    /**
     * LiveRoomState
     */
    private int state;

    private String keywords;

    private Date createdTime;

    private Date updatedTime;
}
