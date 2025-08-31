package com.example.allrabackendassignment.web.dto.internal.cart.request;

public record CartItemsRequestDto(
    Long productId, int quantity
) {
}
