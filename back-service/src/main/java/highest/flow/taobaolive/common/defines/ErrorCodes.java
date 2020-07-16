package highest.flow.taobaolive.common.defines;

public enum ErrorCodes {

    SUCCESS(0),
    INTERNAL_ERROR(101),
    UNKNOWN_ERROR(102),
    UNEXPECTED_CALL(103),
    INVALID_TOKEN(104),

    ALREADY_REGISTERED_USER(201),
    NOT_FOUND_USER(202),
    INVALID_PASSWORD(203),
    UNAUTHORIZED_USER(204), // 权限不够
    UNAUTHORIZED_MACHINE(205), // 不是绑定的机器
    UNAUTHORIZED_LIVEROOM(206), // 不是绑定的直播间

    INVALID_QRCODE_TOKEN(207),
    EXPIRED_QRCODE_TOKEN(208),

    // TAOBAO ERROR CODE
    QRCODE_LOGIN_START(301),
    QRCODE_LOGIN_SCAN_SUCCESS(302),
    FAIL_SYS_ILEGEL_SIGN(303),
    FAIL_SYS_SESSION_EXPIRED(304),   // 过期了
    FAIL_SYS_TOKEN_EMPTY(305),       // 令牌为空
    FAIL_SYS_TOKEN_EXOIRED(306),     // 令牌过期了
    FAIL_CREATE_LIVEROOM_LIMITED(307),   // 达到当天发布预告限制
    FAIL_ADD_ITEM_LIMITED(308),          // 添加商品达到限制
    FAIL_ALREADY_STARTED_LIVEROOM(309);  // 该用户已经有直播内容

    private int code = 0;

    ErrorCodes(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
