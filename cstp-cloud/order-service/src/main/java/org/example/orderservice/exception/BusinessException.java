package org.example.orderservice.exception;


public class BusinessException extends RuntimeException {
    int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
    public int getCode() {
        return code;
    }
}
