package highest.flow.taobaolive.taobao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@TableName("tbl_prelive_template")
public class PreLiveTemplateEntity {

    @TableId(type = IdType.AUTO)
    private int id;

    private int templateId;

    /**
     * LiveRoomKind
     */
    private int liveKind;

    private int liveChannelId;

    private int liveColumnId;

    private int pscChannelId;

    private int pscCategoryId;

    private int pscStartPrice;

    private int pscEndPrice;

    private int pscMinSales;

    private int pscProductCount;

    private boolean pscIsTmall;

    /**
     * ProductSearchSortKind
     */
    private int pscSortKind;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdTime;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updatedTime;

}
