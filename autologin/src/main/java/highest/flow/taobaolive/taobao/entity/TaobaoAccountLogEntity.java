package highest.flow.taobaolive.taobao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("tbl_accounts_logs")
public class TaobaoAccountLogEntity {

    @TableId(type = IdType.AUTO)
    private int id;

    private int memberId;

    // TaobaoAccountLogKind
    private int kind;

    private String uid;

    private String nick;

    private int success;

    private Date expires;

    private String content;

    private Date createdTime;
}
