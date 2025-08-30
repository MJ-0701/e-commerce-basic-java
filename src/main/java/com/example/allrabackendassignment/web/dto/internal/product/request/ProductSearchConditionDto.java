package com.example.allrabackendassignment.web.dto.internal.product.request;


import com.example.allrabackendassignment.domain.product.entity.QProduct;
import com.example.allrabackendassignment.domain.product.entity.QProductCategory;
import com.querydsl.core.BooleanBuilder;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@ToString
public class ProductSearchConditionDto {

    /* ===== 통합 키워드 ===== */
    private final String q;                // 자유 텍스트 (상품명/카테고리/검색텍스트 OR)

    /* ===== 직접 필드 검색(선택) ===== */
    private final String name;             // 상품명 직접 검색
    private final String categoryText;     // 카테고리/검색텍스트 직접 검색

    /* ===== 정형 필터 ===== */
    private final Integer minPrice;        // 가격 하한
    private final Integer maxPrice;        // 가격 상한
    private final Long categoryId;         // leaf 카테고리 ID만
    private final Boolean isActive;        // 상품 활성 여부

    /* ===== 정렬/페이징 ===== */
    private final SortKey sortKey;         // PRICE, NAME, CREATED_AT 등
    private final SortDir sortDir;         // ASC / DESC
    @PositiveOrZero
    private final Integer page;   // 0-based
    @Min(1) @Max(200) private final Integer size;

    @Builder
    public ProductSearchConditionDto(
            String q,
            String name,
            String categoryText,
            Integer minPrice,
            Integer maxPrice,
            Long categoryId,
            Boolean isActive,
            SortKey sortKey,
            SortDir sortDir,
            Integer page,
            Integer size
    ) {
        this.q = trimToNull(q);
        this.name = trimToNull(name);
        this.categoryText = trimToNull(categoryText);
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.categoryId = categoryId;
        this.isActive = isActive;
        this.sortKey = sortKey == null ? SortKey.CREATED_AT : sortKey;
        this.sortDir = sortDir == null ? SortDir.DESC : sortDir;
        this.page = page == null ? 0 : page;
        this.size = size == null ? 20 : size;
    }

    private static String trimToNull(String s) {
        return StringUtils.hasText(s) ? s.trim() : null;
    }

    /* ====== 선택: q 에서 "1000~2000" 같은 가격 패턴 자동 파싱 ====== */
    private static final Pattern PRICE_RANGE = Pattern.compile("(\\d+)\\s*[-~]\\s*(\\d+)");

    /** q 안에 가격 범위가 섞여 들어온 경우를 감지 (컨트롤러/서비스에서 사용) */
    public ProductSearchConditionDto normalizeFromQ() {
        if (q == null) return this;
        Matcher m = PRICE_RANGE.matcher(q);
        if (!m.find()) return this;

        int lo = Integer.parseInt(m.group(1));
        int hi = Integer.parseInt(m.group(2));
        // q 에서 범위를 제거한 텍스트
        String qText = trimToNull(m.replaceAll("").replaceAll("\\s+", " "));
        return ProductSearchConditionDto.builder()
                .q(qText)
                .name(name)
                .categoryText(categoryText)
                .minPrice(this.minPrice != null ? this.minPrice : Math.min(lo, hi))
                .maxPrice(this.maxPrice != null ? this.maxPrice : Math.max(lo, hi))
                .categoryId(categoryId)
                .isActive(isActive)
                .sortKey(sortKey)
                .sortDir(sortDir)
                .page(page)
                .size(size)
                .build();
    }

    /* ====== QueryDSL where 절 헬퍼 ======
       Q 클래스 예: QProduct product, QProductCategory pc
    */
    public BooleanBuilder toPredicate(QProduct product, QProductCategory pc) {
        BooleanBuilder where = new BooleanBuilder();

        // 구조화 필터 (AND)
        if (minPrice != null && maxPrice != null) {
            where.and(product.productPrice.between(minPrice, maxPrice));
        } else if (minPrice != null) {
            where.and(product.productPrice.goe(minPrice));
        } else if (maxPrice != null) {
            where.and(product.productPrice.loe(maxPrice));
        }
        if (categoryId != null)       where.and(product.categoryId.eq(categoryId));
        if (isActive != null)         where.and(product.isActive.eq(isActive));

        // 직접 필드 검색 (AND)
        if (name != null)             where.and(product.productName.contains(name));
        if (categoryText != null) {
            BooleanBuilder catOr = new BooleanBuilder()
                    .or(pc.category.contains(categoryText))
                    .or(pc.upperCategory.contains(categoryText))
                    .or(pc.searchText.contains(categoryText));
            where.and(catOr);
        }

        // 통합 키워드 q (텍스트 필드 OR 묶음)
        if (q != null) {
            BooleanBuilder text = new BooleanBuilder()
                    .or(product.productName.contains(q))
                    .or(pc.category.contains(q))
                    .or(pc.upperCategory.contains(q))
                    .or(pc.searchText.contains(q));
            where.and(text);
        }
        return where;
    }

    public enum SortKey { PRICE, NAME, CREATED_AT }
    public enum SortDir { ASC, DESC }
}
