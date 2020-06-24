package highest.flow.taobaolive.taobao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import highest.flow.taobaolive.common.http.CookieHelper;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updatedTime;

    @TableField(exist = false)
    private CookieStore cookieStore = new BasicCookieStore();

    public String getCookie() {
        List<String> cookieHeaders = new ArrayList<>();
        List<Cookie> cookies = cookieStore.getCookies();
        for (Cookie cookie : cookies) {
            cookieHeaders.add(CookieHelper.toString(cookie));
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(cookieHeaders);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";

    }

    public void mergeCookies(List<Cookie> newCookies) {
        List<Cookie> oldCookies = cookieStore.getCookies();

        cookieStore.clear();

        for (Cookie cookie : oldCookies) {
            Cookie newCookie = cookie;
            for (Cookie cookie1 : newCookies) {
                if (newCookie.getName().compareToIgnoreCase(cookie1.getName()) == 0) {
                    newCookie = cookie1;
                    if (newCookie.getDomain().toLowerCase().compareTo("taobao.com") == 0) {
                        ((BasicClientCookie) newCookie).setDomain(".taobao.com");
                    }
                    break;
                }
            }
            cookieStore.addCookie(newCookie);
        }

        for (Cookie cookie1 : newCookies) {
            Cookie newCookie = cookie1;
            for (Cookie cookie : oldCookies) {
                if (newCookie.getName().compareToIgnoreCase(cookie1.getName()) == 0) {
                    newCookie = null;
                    break;
                }
            }
            if (newCookie != null) {
                if (newCookie.getDomain().toLowerCase().compareTo("taobao.com") == 0) {
                    ((BasicClientCookie) newCookie).setDomain(".taobao.com");
                }
                cookieStore.addCookie(newCookie);
            }
        }
    }

    @JsonIgnore
    public String getToken() {
        List<Cookie> cookies = cookieStore.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().compareTo("_tb_token_") == 0) {
                return cookie.getValue();
            }
        }
        return "";
    }
}
