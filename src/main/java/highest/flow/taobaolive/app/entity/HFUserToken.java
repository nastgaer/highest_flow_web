package highest.flow.taobaolive.app.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("tbl_user_tokens")
public class HFUserToken {

    private int id;

    @TableId(type = IdType.INPUT)
    private String username;

    private String token;

    private Date expireTime;

    private Date updatedTime;
}
