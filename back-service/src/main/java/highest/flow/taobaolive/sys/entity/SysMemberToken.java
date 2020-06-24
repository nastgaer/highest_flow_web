package highest.flow.taobaolive.sys.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("tbl_member_tokens")
public class SysMemberToken {

    @TableId(type = IdType.AUTO)
    private int id;

    private String username;

    private String token;

    private Date expireTime;

    private Date updatedTime;
}
