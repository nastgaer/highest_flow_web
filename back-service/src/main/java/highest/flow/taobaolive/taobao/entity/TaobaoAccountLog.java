package highest.flow.taobaolive.taobao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("tbl_accounts_logs")
public class TaobaoAccountLog {

    private int id;

    // 0: 重登，1 : 延期
    private int category;

    private String accountId;

    private String nick;

    private int expires;

    private String content;

    private Date createdTime;
}
