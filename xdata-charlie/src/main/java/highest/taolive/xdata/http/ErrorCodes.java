package highest.taolive.xdata.http;

public enum ErrorCodes {

    SUCCESS(100),
    FAILED(101),
    INVALID_PARAMETER(200),
    INVALID_APPKEY(300),
    OCCUR_EXCEPTION(400),
    INVALID_URI(500);

    private int code = 100;

    ErrorCodes(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public static ErrorCodes fromInt(int code) {
        for (ErrorCodes errorCodes : ErrorCodes.values()) {
            if (errorCodes.getCode() == code) {
                return errorCodes;
            }
        }
        return ErrorCodes.SUCCESS;
    }
}
