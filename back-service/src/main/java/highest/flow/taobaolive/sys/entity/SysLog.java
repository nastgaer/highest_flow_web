package highest.flow.taobaolive.sys.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("tbl_logs")
public class SysLog {

    @TableId(type = IdType.AUTO)
    private int id;

    private String memberName;

    private String operation;

    private String method;

    private String params;

    private int duration;

    private String ip;

    private Date createdTime;
}
