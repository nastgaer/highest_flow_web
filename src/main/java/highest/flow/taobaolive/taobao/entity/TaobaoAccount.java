package highest.flow.taobaolive.taobao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import highest.flow.taobaolive.common.http.CookieHelper;
import lombok.Data;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;

import java.util.Date;
import java.util.List;

@Data
@TableName("tbl_accounts")
public class TaobaoAccount {

    private int id;

    private String accountId;

    private String nick;

    private String sid;

    private String utdid;

    private String devid;

    private String autoLoginToken;

    private String umidToken;

    private String cookie;

    private Date expires;

    private int state;

    private Date createdTime;

    private Date updatedTime;

    private CookieStore cookieStore = new BasicCookieStore();

    public void setCookie(String value) {
        this.cookie = value;

        String[] cookieHeaders = value.split(";");
        cookieStore.clear();

        for (String cookieHeader : cookieHeaders) {
            cookieStore.addCookie(CookieHelper.decodeCookie(cookieHeader));
        }
    }
}
