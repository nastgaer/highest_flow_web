package highest.flow.taobaolive.common.utils;

import highest.flow.taobaolive.common.defines.ErrorCodes;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

public class R extends HashMap<String, Object> {

    public R() {
        put("code", ErrorCodes.SUCCESS.toString());
        put("msg", "成功");
    }

    public ErrorCodes getCode() {
        String codeStr = (String)get("code");
        return ErrorCodes.valueOf(codeStr);
    }

    public String getMsg() {
        return (String)get("msg");
    }

    public static R error(ErrorCodes code, String msg) {
        R r = new R();
        r.put("code", code.toString());
        r.put("msg", msg);
        return r;
    }

    public static R error() {
        return error(ErrorCodes.INTERNAL_ERROR, "未知异常，请联系管理员");
    }

    public static R error(String msg) {
        return error(ErrorCodes.INTERNAL_ERROR, msg);
    }

    public static R ok(String msg) {
        R r = new R();
        r.put("msg", msg);
        return r;
    }

    public static R ok(Map<String, Object> map) {
        R r = new R();
        r.putAll(map);
        return r;
    }

    public static R ok() {
        return new R();
    }

    public static R ok(Object data) {
        return new R().put("data", data);
    }

    public R put(String key, Object value) {
        super.put(key, value);
        return this;
    }
}
