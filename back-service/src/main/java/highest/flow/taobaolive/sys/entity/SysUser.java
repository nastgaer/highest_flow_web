package highest.flow.taobaolive.sys.entity;

import lombok.Data;

import java.util.Date;

@Data
public class SysUser {

    private int id;

    private String userId;

    private String password;

    private String salt;

    private String machineCode;

    private String mobile;

    private int state;

    private Date createdTime;

    private Date updatedTime;
}
