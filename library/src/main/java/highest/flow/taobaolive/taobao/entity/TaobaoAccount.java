package highest.flow.taobaolive.taobao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import highest.flow.taobaolive.common.http.CookieHelper;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.BasicCookieStore;

import java.util.Date;

@Data
@TableName("tbl_accounts")
public class TaobaoAccount {

    @TableId
    private int id;

    private String accountId;

    private String nick;

    private String sid;

    private String uid;

    private String utdid;

    private String devid;

    private String autoLoginToken;

    private String umidToken;

    @Getter
    @Setter(AccessLevel.NONE)
    private String cookie;

    private Date expires;

    /**
     * TaobaoAccountState
     */
    private int state;

    private Date createdTime;

    private Date updatedTime;

    @TableField(exist = false)
    private CookieStore cookieStore = new BasicCookieStore();
}
