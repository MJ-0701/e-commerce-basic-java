package com.example.allrabackendassignment.domain.cart.repository;

import com.example.allrabackendassignment.domain.cart.entity.Cart;
import com.example.allrabackendassignment.domain.cart.entity.CartItems;
import com.example.allrabackendassignment.domain.cart.repository.custom.CartItemsCustomRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CartItemsRepository extends JpaRepository<CartItems, Long>, CartItemsCustomRepository {
    @Query("SELECT ci FROM CartItems ci WHERE ci.cartId = :cartId AND ci.productId = :productId")
    CartItems findByCartId(@Param("cartId") Long cartId, @Param("productId") Long productId);
}
