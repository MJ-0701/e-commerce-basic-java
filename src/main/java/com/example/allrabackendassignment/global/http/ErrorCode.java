package com.example.allrabackendassignment.global.http;

import lombok.Getter;

@Getter
public enum ErrorCode {
    CART_EMPTY("CART_EMPTY", "장바구니가 비어있습니다."),
    PRODUCT_NOT_FOUND("PRODUCT_NOT_FOUND", "상품을 찾을 수 없습니다."),
    PRODUCT_INACTIVE("PRODUCT_INACTIVE", "판매 중지된 상품입니다."),
    OUT_OF_STOCK("OUT_OF_STOCK", "상품 재고가 없습니다."),
    INSUFFICIENT_STOCK("INSUFFICIENT_STOCK", "요청 수량이 재고보다 많습니다.");

    private final String code;
    private final String message;

    ErrorCode(String code, String message){
        this.code = code;
        this.message = message;
    }
}
