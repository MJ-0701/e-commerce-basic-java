package com.example.allrabackendassignment.web.dto.internal.order.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public record OrderItemRequest(
        List<CartItemRequest> items
) {

    public record CartItemRequest(
            Long productId,
            String productName,
            int quantity,
            int unitPrice,
            int totalPrice
    ) {}

    // 합계 구하는 메서드
    public int totalOrderPrice() {
        if (items == null) return 0;
        return items.stream()
                .mapToInt(CartItemRequest::totalPrice)
                .sum();
    }
}
