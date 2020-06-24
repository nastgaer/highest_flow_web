package highest.flow.taobaolive.common.http;

import org.apache.commons.lang3.StringUtils;

import java.net.URL;

public class HttpUtils {

    /**
     * 从网址中获取host
     * @param url 网址
     * @return 失败返回null
     */
    public static String getUrlHost(String url) {
        if (StringUtils.isNoneBlank(url)) {
            try {
                URL u = new URL(url);
                return u.getHost();
            } catch (Exception e) {
            }
        }
        return null;
    }
}
