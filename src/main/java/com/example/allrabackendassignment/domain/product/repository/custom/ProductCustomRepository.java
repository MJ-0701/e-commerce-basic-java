package com.example.allrabackendassignment.domain.product.repository.custom;

import com.example.allrabackendassignment.web.dto.internal.product.request.ProductSearchConditionDto;
import com.example.allrabackendassignment.web.dto.internal.product.response.ProductSearchResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductCustomRepository {

    public Page<ProductSearchResponseDto> searchProduct(ProductSearchConditionDto productSearchConditionDto, Pageable pageable);
}
