package com.example.allrabackendassignment.web.controller.product;

import com.example.allrabackendassignment.global.common.PageResponse;
import com.example.allrabackendassignment.global.http.ResponseObject;
import com.example.allrabackendassignment.service.ProductService;
import com.example.allrabackendassignment.web.dto.internal.product.request.ProductSearchConditionDto;
import com.example.allrabackendassignment.web.dto.internal.product.response.ProductSearchResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/product")
@RestController
@RequiredArgsConstructor
public class ProductRestApiController {

    private final ProductService productService;

    @PostMapping("/search")
    public ResponseEntity<ResponseObject<PageResponse<ProductSearchResponseDto>>> getSearchProductList(
            @RequestBody ProductSearchConditionDto productSearchConditionDto,
            Pageable pageable) {
        return ResponseEntity.ok().body(ResponseObject.of(productService.getProductList(productSearchConditionDto, pageable)));
    }
}
