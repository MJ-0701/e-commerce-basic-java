package com.example.allrabackendassignment.web.dto.internal.product.response;

public record ProductSearchResponseDto(Long productId, String productName, String productThumbnail,
                                       int productPrice, String upperCategory, String category, String searchText, Boolean isActive ) {
}
