package highest.flow.taobaolive.taobao.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import highest.flow.taobaolive.common.utils.CommonUtils;
import highest.flow.taobaolive.common.utils.HFStringUtils;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;

import java.util.Date;
import java.util.List;

@Data
public class H5Header {

    private String appKey = "12574478";// "25443018";

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private long timestamp = 0;

    private String token = "";

    @Getter
    @Setter(AccessLevel.NONE)
    private boolean expired = false;

    public H5Header(TaobaoAccountEntity taobaoAccountEntity) {
        CookieStore cookieStore = taobaoAccountEntity.getCookieStore();
        List<Cookie> cookies = cookieStore.getCookies();

        String h5tk = "";
        for (Cookie cookie : cookies) {
            if (cookie.getName().compareTo("_m_h5_tk") == 0) {
                h5tk = cookie.getValue();
                break;
            }
        }

        if (HFStringUtils.isNullOrEmpty(h5tk)) {
            return;
        }

        String[] words = h5tk.split("_");
        if (words.length < 2) {
            return;
        }

        token = words[0];

        timestamp = Long.parseLong(words[1]);
        Date date = CommonUtils.timestampToDate(timestamp);
        long now = CommonUtils.dateToTimestamp(new Date());
        if (timestamp < now) {
            expired = true;
            return;
        }
    }

    public H5Header(TaobaoAccountEntity taobaoAccountEntity, String appKey) {
        this(taobaoAccountEntity);
        this.appKey = appKey;
    }

    @JsonIgnore
    public long getLongTimestamp() {
        return timestamp;
    }

    @JsonIgnore
    public long getShortTimestamp() {
        return timestamp / 1000;
    }
}
