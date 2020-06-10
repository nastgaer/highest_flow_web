package highest.flow.taobaolive.app.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("tbl_users")
public class HFUser {

    @TableId(type = IdType.AUTO)
    private int id;

    private String username;

    private String password;

    private String salt;

    private String machineCode;

    private String mobile;

    private String weixin;

    private int level;

    private int serviceType;

    private int state;

    private String code;

    private String accountId;

    private Date createdTime;

    private Date updatedTime;

}
