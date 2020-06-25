package highest.flow.taobaolive.taobao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("tbl_products")
public class Product {

    @TableId(type = IdType.AUTO)
    private int id;

    private int categoryId;

    private String categoryName = "";

    private String productId = "";;

    private String title = "";;

    private String price = "";;

    private int monthSales;

    private String shopName = "";;

    private String url = "";;

    private String picurl = "";;

    private int state;

    private String remark = "";;

    @TableField(exist = false)
    private long timepoint;

    private Date createdTime;

    private Date updatedTime;

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
