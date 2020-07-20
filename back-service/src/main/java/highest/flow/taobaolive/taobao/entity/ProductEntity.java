package highest.flow.taobaolive.taobao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@TableName("tbl_liveroom_products")
public class ProductEntity {

    @TableId(type = IdType.AUTO)
    private int id;

    private String liveId;

    private String categoryId;

    private String categoryTitle = "";

    private String productId = "";;

    private String title = "";;

    private String price = "";;

    private int monthSales;

    private String shopName = "";;

    private String url = "";;

    private String picurl = "";;

    /**
     * ProductState
     */
    private int state;

    /**
     * 备注
     */
    private String remark = "";

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdTime;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updatedTime;

    @TableField(exist = false)
    /**
     * 讲解
     */
    private long timepoint;

    @TableField(exist = false)
    private int businessSceneId = 0;

    @TableField(exist = false)
    private String pg1stepk = "";

    @TableField(exist = false)
    private String scm = "";

    @TableField(exist = false)
    private String spm = "";

    @TableField(exist = false)
    private String bizType = "";

    @TableField(exist = false)
    private String liveInfo = "";

    @TableField(exist = false)
    private String utparam = "";

    @TableField(exist = false)
    private String descVersion = "";
}
