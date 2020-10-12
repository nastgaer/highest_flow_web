package com.useful.server.entity;

public enum ErrorCodes {
    SUCCESS(100),
    FAILED(101),
    INVALID_COMMAND(102),
    NO_CLIENT(103);

    int code = 0;
    ErrorCodes(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }
}
