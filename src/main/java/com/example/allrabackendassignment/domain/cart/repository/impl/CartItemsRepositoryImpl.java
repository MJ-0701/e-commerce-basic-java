package com.example.allrabackendassignment.domain.cart.repository.impl;

import com.example.allrabackendassignment.domain.cart.entity.CartItems;
import com.example.allrabackendassignment.domain.cart.repository.custom.CartItemsCustomRepository;
import com.example.allrabackendassignment.global.common.support.QueryDslPageAndSort;
import com.example.allrabackendassignment.web.dto.internal.cart.response.CartItemsResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;

import static com.example.allrabackendassignment.domain.cart.entity.QCart.cart;
import static com.example.allrabackendassignment.domain.cart.entity.QCartItems.cartItems;
import static com.example.allrabackendassignment.domain.product.entity.QProduct.product;

public class CartItemsRepositoryImpl extends QueryDslPageAndSort implements CartItemsCustomRepository {

    private final JPAQueryFactory queryFactory;

    public CartItemsRepositoryImpl(EntityManager em,JPAQueryFactory queryFactory) {
        super(em, CartItems.class);
        this.queryFactory = queryFactory;
    }

    @Override
    public Slice<CartItemsResponse> userCartItemList(Pageable pageable, Long userId) {

        // pageSize + 1 건 가져와서 hasNext 판단
        List<CartItemsResponse> results = queryFactory
                .select(Projections.constructor(
                        CartItemsResponse.class,
                        product.productName,    // String productName
                        product.productThumbnail,      // String productThumbnail
                        cartItems.productId,    // Long productId
                        product.isActive,       // Boolean isActive
                        product.productPrice,   // int productPrice
                        cartItems.quantity      // int quantity
                ))
                .from(cartItems)
                .join(cart).on(cartItems.cartId.eq(cart.cart_id))
                .join(product).on(product.productId.eq(cartItems.productId))
                .where(cart.user_id.eq(userId))
                .orderBy(cartItems.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = results.size() > pageable.getPageSize();
        if (hasNext) {
            results.remove(results.size() - 1); // 초과건 제거
        }
        return new SliceImpl<>(results, pageable, hasNext);
    }
}
