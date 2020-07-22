package highest.flow.taobaolive.taobao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@TableName("tbl_accounts")
public class TaobaoAccountEntity {

    @TableId(type = IdType.AUTO)
    private int id;

    private int memberId;

    private String nick;

    private String sid;

    private String uid;

    private String utdid;

    private String devid;

    @JsonIgnore
    private String autoLoginToken;

    private String umidToken;

    @Getter
    @Setter(AccessLevel.NONE)
    @JsonIgnore
    private String cookie;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expires;

    /**
     * TaobaoAccountState
     */
    private int state;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdTime;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updatedTime;

    @TableField(exist = false)
    @JsonIgnore
    private CookieStore cookieStore = new BasicCookieStore();

//    public String getCookie() {
//        List<String> cookieHeaders = new ArrayList<>();
//        List<Cookie> cookies = cookieStore.getCookies();
//        for (Cookie cookie : cookies) {
//            cookieHeaders.add(CookieHelper.toString(cookie));
//        }
//
//        try {
//            ObjectMapper objectMapper = new ObjectMapper();
//            return objectMapper.writeValueAsString(cookieHeaders);
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return "";
//    }

    public void setCookie(String cookieHeaders) {
        this.cookie = cookieHeaders;

        // String to CookieStore
        this.cookieStore.clear();

        JsonParser jsonParser = JsonParserFactory.getJsonParser();
        List<Object> texts = jsonParser.parseList(cookieHeaders);
        for (Object obj : texts) {
            String cookieHeader = (String) obj;
            Cookie cookie = CookieHelper.parseString(cookieHeader);
            if (cookie != null) {
                this.cookieStore.addCookie(cookie);
            }
        }
    }

    public void setCookieStore(CookieStore cookieStore) {
        this.cookieStore = cookieStore;

        // CookieStore to String
        List<String> cookieHeaders = new ArrayList<>();
        List<Cookie> cookies = cookieStore.getCookies();
        for (Cookie cookie : cookies) {
            cookieHeaders.add(CookieHelper.toString(cookie));
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            this.cookie = objectMapper.writeValueAsString(cookieHeaders);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void mergeCookies(List<Cookie> newCookies) {
        List<Cookie> oldCookies = cookieStore.getCookies();

        CookieStore newCookieStore = new BasicCookieStore();

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
            newCookieStore.addCookie(newCookie);
        }

        for (Cookie cookie1 : newCookies) {
            Cookie newCookie = cookie1;
            for (Cookie cookie : oldCookies) {
                if (newCookie.getName().compareToIgnoreCase(cookie.getName()) == 0) {
                    newCookie = null;
                    break;
                }
            }
            if (newCookie != null) {
                if (newCookie.getDomain().toLowerCase().compareTo("taobao.com") == 0) {
                    ((BasicClientCookie) newCookie).setDomain(".taobao.com");
                }
                newCookieStore.addCookie(newCookie);
            }
        }

        this.setCookieStore(newCookieStore);
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
