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

    NOT_FOUND_LICENSE_CODE(207),
    EXPIRED_CODE(208),
    NOT_FOUND_TAOBAO_ACCOUNT(209),

    // TAOBAO ERROR CODE
    QRCODE_LOGIN_START(301),
    FAIL_SYS_SESSION_EXPIRED(302),   // 过期了
    FAIL_SYS_TOKEN_EMPTY(303),       // 令牌为空
    FAIL_SYS_TOKEN_EXOIRED(304),     // 令牌过期了
    FAIL_CREATE_LIVEROOM_LIMITED(305),   // 达到当天发布预告限制
    FAIL_ADD_ITEM_LIMITED(306),          // 添加商品达到限制
    FAIL_ALREADY_STARTED_LIVEROOM(307);  // 该用户已经有直播内容

    private int code = 0;

    ErrorCodes(int code) {
        this.code = code;
    }
}
