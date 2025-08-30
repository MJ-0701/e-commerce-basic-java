package com.example.allrabackendassignment.web.dto.internal.cart.response;

public record CartItemsResponse(String productName, String productThumbnail, Long productId, Boolean isActive, int productPrice,
                                int quantity) {
}
