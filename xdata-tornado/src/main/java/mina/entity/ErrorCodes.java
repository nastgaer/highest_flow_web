package mina.entity;

public enum ErrorCodes {

    SUCCESS(100),
    INTERNAL_ERROR(101),
    INVALID_COMMAND(102);

    private int code = 0;

    ErrorCodes(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
