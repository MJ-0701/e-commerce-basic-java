package com.example.allrabackendassignment.global.http.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(Long productId, int requested, int available) {
        super("재고 부족: productId=" + productId + ", 요청수량=" + requested + ", 현재재고=" + available);
    }
}
