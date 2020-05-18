package highest.flow.taobaolive.app.entity;

import lombok.Data;

import java.util.Date;

@Data
public class HFUser {

    private int id;

    private String userId;

    private String password;

    private String machineCode;

    private String mobile;

    private String weixin;

    private int level;

    private int serviceType;

    private int state;

    private Date serviceStartTime;

    private Date serviceEndTime;

    private Date createdTime;

    private Date updatedTime;

}
