package highest.flow.taobaolive.xiaohao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("tbl_accounts")
public class TaobaoAccount {

    private int id;

    private String accountId;

    private String nick;

    private String sid;

    private String utdid;

    private String devid;

    private String autoLoginToken;

    private String cookie;

    private Date expires;

    private int state;

    private Date createdTime;

    private Date updatedTime;
}
