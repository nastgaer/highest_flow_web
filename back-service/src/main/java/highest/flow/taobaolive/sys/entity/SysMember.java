package highest.flow.taobaolive.sys.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("tbl_members")
public class SysMember {

    @TableId(type = IdType.AUTO)
    private int id;

    private String username;

    private String password;

    private String salt;

    private String mobile;

    private String comment;

    private int state;

    private Date createdTime;

    private Date updatedTime;
}
