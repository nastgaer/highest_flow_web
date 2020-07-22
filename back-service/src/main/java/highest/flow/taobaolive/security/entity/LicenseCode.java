package highest.flow.taobaolive.security.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import highest.flow.taobaolive.security.defines.LicenseCodeState;
import lombok.Data;

import java.util.Date;

@Data
@TableName("tbl_codes")
public class LicenseCode {

    @TableId(type = IdType.AUTO)
    private int id;

    /**
     * LicenseCodeType
     */
    private int codeType;

    /**
     * MemberServiceType
     */
    private int serviceType;

    private int hours;

    private String code = "";

    /**
     * LicenseCodeState
     */
    private int state;

    private String machineCode = "";

    private int memberId;

    private Date serviceStartTime;

    private Date serviceEndTime;

    private Date createdTime;

    private Date acceptedTime;
}
