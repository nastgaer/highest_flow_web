package highest.flow.taobaolive.taobao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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

    private String categoryName;

    private String productId;

    private String title;

    private String price;

    private int monthSales;

    private String shopName;

    private String url;

    private String picurl;

    private int state;

    private String remark;

    private Date createdTime;

    private Date updatedTime;
}
