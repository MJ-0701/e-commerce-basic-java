package com.example.allrabackendassignment.web.dto.internal.product.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.util.StringUtils;

/**
 * @param q            ===== 통합 키워드 =====  자유 텍스트 (상품명/카테고리/검색텍스트 OR)
 * @param name         ===== 직접 필드 검색(선택) =====  상품명 직접 검색
 * @param categoryText 카테고리/검색텍스트 직접 검색
 * @param minPrice     ===== 정형 필터 =====  가격 하한
 * @param maxPrice     가격 상한
 * @param categoryId   카테고리 ID만
 * @param isActive     상품 활성 여부
 */
public record ProductSearchConditionDto(String q, String name, String categoryText, Integer minPrice, Integer maxPrice,
                                        Long categoryId, Boolean isActive) {

    @Builder
    public ProductSearchConditionDto(
            String q,
            String name,
            String categoryText,
            Integer minPrice,
            Integer maxPrice,
            Long categoryId,
            Boolean isActive
    ) {
        this.q = trimToNull(q);
        this.name = trimToNull(name);
        this.categoryText = trimToNull(categoryText);
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.categoryId = categoryId;
        this.isActive = isActive;
    }

    private static String trimToNull(String s) {
        return StringUtils.hasText(s) ? s.trim() : null;
    }
}