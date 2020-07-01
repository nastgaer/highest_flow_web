package highest.flow.taobaolive.taobao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("tbl_member_tcc")
public class MemberTaoAccEntity {

    private int id;

    private int memberId;

    private String taobaoAccountId;

    private String roomName;

    private String taocode;

    private Date createdTime;
}
