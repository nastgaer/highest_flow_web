package highest.flow.taobaolive.taobao.entity;

import highest.flow.taobaolive.common.defines.ErrorCodes;

import java.util.List;
import java.util.Map;

public class TaobaoReturn {

    private ErrorCodes errorCode = ErrorCodes.SUCCESS;
    private String errorMsg = "";

    public TaobaoReturn(Map<String, Object> respMap) {

        String errorCodeStr = "SUCCESS";
        if (respMap.containsKey("httpStatusCode")) { // H5 API
            errorCodeStr = (String) respMap.get("msgCode");
            errorMsg = (String) respMap.get("msgInfo");
        } else { // MOBILE API
            List<String> retList = respMap.containsKey("ret") ? (List<String>) respMap.get("ret") : null;
            if (retList != null) {
                for (String key : retList) {
                    String[] words = key.split("::");
                    if (words.length >= 2) {
                        errorCodeStr = words[0];
                        errorMsg = words[1];
                    }
                }
            }
        }

        boolean found = false;
        for (ErrorCodes errorCode : ErrorCodes.values()) {
            if (errorCode.toString().equals(errorCodeStr)) {
                this.errorCode = errorCode;
                found = true;
                break;
            }
        }

        if (!found) {
            this.errorCode = ErrorCodes.UNKNOWN_ERROR;
        }

        if (errorMsg.contains("发布宝贝个数超出限制")) {
            this.errorCode = ErrorCodes.FAIL_ADD_ITEM_LIMITED;
        } else if (errorMsg.contains("该用户已经有直播内容")) {
            this.errorCode = ErrorCodes.FAIL_ALREADY_STARTED_LIVEROOM;
        }
    }

    public ErrorCodes getErrorCode() {
        return this.errorCode;
    }

    public String getErrorMsg() {
        return this.errorMsg;
    }
}
