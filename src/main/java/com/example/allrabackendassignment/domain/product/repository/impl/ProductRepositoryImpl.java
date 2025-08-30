package com.example.allrabackendassignment.domain.product.repository.impl;

import com.example.allrabackendassignment.domain.product.entity.Product;
import com.example.allrabackendassignment.domain.product.entity.QProduct;
import com.example.allrabackendassignment.domain.product.entity.QProductCategory;
import com.example.allrabackendassignment.domain.product.repository.custom.ProductCustomRepository;
import com.example.allrabackendassignment.domain.product.repository.support.QueryDslPageAndSort;
import com.example.allrabackendassignment.web.dto.internal.product.request.ProductSearchConditionDto;
import com.example.allrabackendassignment.web.dto.internal.product.response.ProductSearchResponseDto;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProductRepositoryImpl extends QueryDslPageAndSort implements ProductCustomRepository {

    private final JPAQueryFactory queryFactory;

    private static final QProduct p = QProduct.product;
    private static final QProductCategory pc = QProductCategory.productCategory;

    public ProductRepositoryImpl(EntityManager em, JPAQueryFactory queryFactory) {
        super(em, Product.class);
        this.queryFactory = queryFactory;
    }

    @Override
    public Page<ProductSearchResponseDto> searchProduct(ProductSearchConditionDto cond, Pageable pageable) {
        // 1) q에서 "1000~2000" 패턴을 가격 필터로 흡수
        ProductSearchConditionDto c = normalizeFromQ(cond);

        // 2) where 조건 생성 (AND 필터 + 텍스트 OR 묶음)
        BooleanBuilder where = buildWhere(c);

        // 3) 기본 셀렉트 쿼리 구성
        JPQLQuery<ProductSearchResponseDto> query = queryFactory
                .select(Projections.constructor(
                        ProductSearchResponseDto.class,
                        p.productId,
                        p.productName,
                        p.productThumbnail,
                        p.productPrice,
                        pc.upperCategory,
                        pc.category,
                        pc.searchText,
                        p.isActive
                ))
                .from(p)
                .join(pc).on(p.categoryId.eq(pc.categoryId))
                .where(where);

        // 4) 페이징/정렬 적용 + Page 생성
        return getCustomPageImpl(pageable, query);
    }

    /* =========================
       Private helpers
       ========================= */

    private BooleanBuilder buildWhere(ProductSearchConditionDto c) {
        BooleanBuilder where = new BooleanBuilder();
        if (c == null) return where;

        // 구조화 필터 (AND)
        if (c.minPrice() != null && c.maxPrice() != null) {
            where.and(p.productPrice.between(c.minPrice(), c.maxPrice()));
        } else if (c.minPrice() != null) {
            where.and(p.productPrice.goe(c.minPrice()));
        } else if (c.maxPrice() != null) {
            where.and(p.productPrice.loe(c.maxPrice()));
        }

        if (c.categoryId() != null) where.and(p.categoryId.eq(c.categoryId()));
        if (c.isActive() != null)   where.and(p.isActive.eq(c.isActive()));

        // 직접 필드 (AND)
        if (c.name() != null) where.and(p.productName.contains(c.name()));
        if (c.categoryText() != null) {
            where.and(
                    new BooleanBuilder()
                            .or(pc.category.contains(c.categoryText()))
                            .or(pc.upperCategory.contains(c.categoryText()))
                            .or(pc.searchText.contains(c.categoryText()))
            );
        }

        // 통합 키워드 q (텍스트 OR)
        if (c.q() != null) {
            where.and(
                    new BooleanBuilder()
                            .or(p.productName.contains(c.q()))
                            .or(pc.category.contains(c.q()))
                            .or(pc.upperCategory.contains(c.q()))
                            .or(pc.searchText.contains(c.q()))
            );
        }

        return where;
    }

    /** q 안의 "1000~2000" → min/maxPrice로 흡수 (옵션). */
    private ProductSearchConditionDto normalizeFromQ(ProductSearchConditionDto src) {
        if (src == null || src.q() == null) return src;

        Pattern range = Pattern.compile("(\\d+)\\s*[-~]\\s*(\\d+)");
        Matcher m = range.matcher(src.q());
        if (!m.find()) return src;

        int lo = Integer.parseInt(m.group(1));
        int hi = Integer.parseInt(m.group(2));

        String qText = src.q().replaceAll(range.pattern(), "").replaceAll("\\s+", " ").trim();
        if (qText.isEmpty()) qText = null;

        return new ProductSearchConditionDto(
                qText,
                src.name(),
                src.categoryText(),
                (src.minPrice() != null ? src.minPrice() : Math.min(lo, hi)),
                (src.maxPrice() != null ? src.maxPrice() : Math.max(lo, hi)),
                src.categoryId(),
                src.isActive()
        );
    }
}