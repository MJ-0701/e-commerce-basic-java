package com.example.allrabackendassignment.global.http;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    CART_EMPTY("CART_EMPTY", "장바구니가 비어있습니다."),
    PRODUCT_NOT_FOUND("PRODUCT_NOT_FOUND", "상품을 찾을 수 없습니다."),
    PRODUCT_INACTIVE("PRODUCT_INACTIVE", "판매 중지된 상품입니다."),
    OUT_OF_STOCK("OUT_OF_STOCK", "상품 재고가 없습니다."),
    INSUFFICIENT_STOCK("INSUFFICIENT_STOCK", "요청 수량이 재고보다 많습니다."),

    // --- 결제 관련 ---
    PAYMENT_FAILED("PAYMENT_FAILED", "결제가 실패했습니다.", HttpStatus.PAYMENT_REQUIRED), // 402
    PAYMENT_TIMEOUT("PAYMENT_TIMEOUT", "결제가 제한 시간 내에 완료되지 않았습니다.", HttpStatus.GATEWAY_TIMEOUT), // 504
    PAYMENT_GATEWAY_ERROR("PAYMENT_GATEWAY_ERROR", "PG사 오류가 발생했습니다.", HttpStatus.BAD_GATEWAY), // 502
    PG_BAD_REQUEST("PG_BAD_REQUEST", "PG 요청 형식이 올바르지 않습니다.", HttpStatus.BAD_REQUEST), // 400
    PG_ERROR("PG_ERROR", "PG 통신 중 오류가 발생했습니다.", HttpStatus.BAD_GATEWAY); // 502

    private final String code;
    private final String message;
    private final HttpStatus status;

    // HttpStatus를 명시하지 않은 경우 기본 400
    ErrorCode(String code, String message) {
        this(code, message, HttpStatus.BAD_REQUEST);
    }

    ErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}