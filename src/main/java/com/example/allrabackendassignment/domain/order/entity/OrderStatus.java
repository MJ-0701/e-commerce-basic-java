package com.example.allrabackendassignment.domain.order.entity;

public enum OrderStatus {
    CREATED("CREATED", "주문생성"),
    PENDING("PENDING", "결제 대기"),
    COMPLETED("COMPLETED", "결제 완료"),
    FAILED("FAILED", "결제 실패"),
    PARTIAL_CANCELLED("PARTIAL_CANCELLED", "부분환불"),
    CANCELED("CANCELED", "주문 취소");

    private final String code;
    private final String description;

    OrderStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() { return code; }
    public String getDescription() { return description; }

}
