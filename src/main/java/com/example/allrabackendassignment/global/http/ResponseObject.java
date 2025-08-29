package com.example.allrabackendassignment.global.http;

import lombok.Getter;

@Getter
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

}
