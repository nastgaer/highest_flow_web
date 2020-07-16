package highest.flow.taobaolive.taobao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@TableName("tbl_member_tcc")
public class MemberTaoAccEntity {

    @TableId(type = IdType.AUTO)
    private int id;

    private int memberId;

    private String taobaoAccountNick;

    private String roomName;

    private String comment;

    /**
     * ServiceState
     */
    private int state;

    private Date serviceStartDate;

    private Date serviceEndDate;

    @TableField(exist = false)
    private List<PreLiveRoomSpecEntity> preLiveRoomSpecs;

    private Date createdTime;

    private Date updatedTime;
}
