package com.example.allrabackendassignment.http;

public class ResponseObject<T> {

    private final int code;
    private final String message;
    private final T result;

    private ResponseObject(int code, String message, T result) {
        this.code = code;
        this.message = message;
        this.result = result;
    }

    public static <T> ResponseObject<T> of(T result) {
        return new ResponseObject<>(
                ResponseCode.SUCCESS.getCode(),
                ResponseCode.SUCCESS.getMessage(),
                result
        );
    }

    public static <T> ResponseObject<T> error(int code, String message) {
        return new ResponseObject<>(code, message, null);
    }

    // getter
    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getResult() {
        return result;
    }
}
