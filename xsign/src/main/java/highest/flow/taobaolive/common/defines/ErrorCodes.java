package highest.flow.taobaolive.common.defines;

public enum ErrorCodes {

    SUCCESS(0),
    INTERNAL_ERROR(101),
    INVALID_PARAMETER(102);

    private int code = 0;

    ErrorCodes(int code) {
        this.code = code;
    }
}
