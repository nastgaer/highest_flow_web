package highest.flow.taobaolive.common.utils;

import org.springframework.http.HttpStatus;

import java.util.HashMap;

public class R extends HashMap<String, Object> {

    public R() {
        put("code", 0);
        put("msg", "");
    }

    public static R error(int code, String msg) {
        R r = new R();
        r.put("code", code);
        r.put("msg", msg);
        return r;
    }

    public static R error() {
        return error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "未知异常，请联系管理员");
    }

    public static R error(String msg) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR.value(), msg);
    }

    public static R ok(String msg) {
        R r = new R();
        r.put("msg", msg);
        return r;
    }

    public static R ok(Object data) {
        return new R().put("data", data);
    }

    public R put(String key, Object value) {
        super.put(key, value);
        return this;
    }
}
