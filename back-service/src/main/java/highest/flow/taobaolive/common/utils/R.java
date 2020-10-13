package highest.flow.taobaolive.common.utils;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import highest.flow.taobaolive.common.defines.ErrorCodes;

import java.util.HashMap;
import java.util.Map;

public class R {

    private ErrorCodes code = ErrorCodes.SUCCESS;

    private String msg = "成功";

    private Map<String, Object> data = new HashMap<>();

    public ErrorCodes getCode() {
        return code;
    }

    public void setCode(ErrorCodes errorCode) {
        this.code = errorCode;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public static R error(ErrorCodes code, String msg) {
        R r = new R();
        r.setCode(code);
        r.setMsg(msg);
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
        r.setMsg(msg);
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

    public Object get(Object key) {
        return data.get(key);
    }

    public R put(String key, Object value) {
        data.put(key, value);
        return this;
    }
}
