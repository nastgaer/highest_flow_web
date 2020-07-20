package highest.flow.taobaolive.sys.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("sys_log")
public class SysLogEntity {

    @TableId(type = IdType.AUTO)
    private int id;

    private String memberName;

    private String operation;

    private String method;

    private String params;

    private String result;

    private long duration;

    private String ip;

    private Date createdTime;
}
