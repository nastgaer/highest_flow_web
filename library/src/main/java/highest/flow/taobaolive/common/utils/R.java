package highest.flow.taobaolive.common.utils;

import highest.flow.taobaolive.common.defines.ErrorCodes;

import java.util.HashMap;
import java.util.Map;

public class R extends HashMap<String, Object> {

    private Map<String, Object> data = new HashMap<>();

    public R() {
        super.put("code", ErrorCodes.SUCCESS.toString());
        super.put("msg", "成功");
        super.put("data", data);
    }

    public ErrorCodes getCode() {
        String codeStr = (String) super.get("code");
        return ErrorCodes.valueOf(codeStr);
    }

    public String getMsg() {
        return (String) super.get("msg");
    }

    public Map<String, Object> getData() {
        return (Map<String, Object>) super.get("data");
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
        r.getData().putAll(map);
        return r;
    }

    public static R ok() {
        return new R();
    }

    public static R ok(Object data) {
        return new R().put("data", data);
    }

    @Override
    public Object get(Object key) {
        return getData().get(key);
    }

    public R put(String key, Object value) {
        this.getData().put(key, value);
        return this;
    }
}
