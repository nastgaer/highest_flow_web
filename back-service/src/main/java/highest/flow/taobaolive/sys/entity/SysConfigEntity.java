package highest.flow.taobaolive.sys.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_config")
public class SysConfigEntity {

    private String paramKey;

    private String paramValue;
}
