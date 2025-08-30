package com.example.allrabackendassignment.service;

import com.example.allrabackendassignment.domain.product.repository.ProductRepository;
import com.example.allrabackendassignment.global.common.PageResponse;
import com.example.allrabackendassignment.web.dto.internal.product.request.ProductSearchConditionDto;
import com.example.allrabackendassignment.web.dto.internal.product.response.ProductSearchResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public PageResponse<ProductSearchResponseDto> getProductList(ProductSearchConditionDto cond, Pageable pageable) {
        return PageResponse.from(productRepository.searchProduct(cond, pageable));
    }
}
