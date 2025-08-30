package com.example.allrabackendassignment.domain.product.repository.custom;

import com.example.allrabackendassignment.web.dto.internal.product.request.ProductSearchConditionDto;
import com.example.allrabackendassignment.web.dto.internal.product.response.ProductSearchResponseDto;

public interface ProductCustomRepository {

    public ProductSearchResponseDto searchProduct(ProductSearchConditionDto productSearchConditionDto);
}
