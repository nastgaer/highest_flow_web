package highest.flow.taobaolive.common.defines;

public enum ErrorCodes {

    SUCCESS,
    INTERNAL_ERROR,
    UNKNOWN_ERROR,
    INVALID_TOKEN,

    ALREADY_REGISTERED_USER,
    NOT_FOUND_USER,
    UNAUTHORIZED_USER,
    UNAUTHORIZED_MACHINE,

    QRCODE_LOGIN_START,
    FAIL_SYS_SESSION_EXPIRED,   // 过期了
    FAIL_SYS_TOKEN_EMPTY,       // 令牌为空
    FAIL_SYS_TOKEN_EXOIRED,     // 令牌过期了
    FAIL_CREATE_LIVEROOM_LIMITED,   // 达到当天发布预告限制
    FAIL_ADD_ITEM_LIMITED,          // 添加商品达到限制
    FAIL_ALREADY_STARTED_LIVEROOM;  // 该用户已经有直播内容
}
