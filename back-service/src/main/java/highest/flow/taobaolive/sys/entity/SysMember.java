package highest.flow.taobaolive.sys.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@TableName("tbl_members")
public class SysMember {

    @TableId(type = IdType.AUTO)
    private int id;

    private String memberName;

    @JsonIgnore
    private String password;

    private int level;

    @JsonIgnore
    private String salt;

    private String mobile;

    private String comment;

    private int state;

    @TableField(exist = false)
    private List<String> roles;

    private Date createdTime;

    private Date updatedTime;
}
