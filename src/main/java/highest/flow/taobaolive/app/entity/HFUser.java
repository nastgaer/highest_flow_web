package highest.flow.taobaolive.app.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("tbl_users")
public class HFUser {

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

    private Date serviceStartTime;

    private Date serviceEndTime;

    private Date createdTime;

    private Date updatedTime;

}
