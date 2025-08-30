package com.example.allrabackendassignment.domain.cart.repository;

import com.example.allrabackendassignment.domain.cart.entity.Cart;
import com.example.allrabackendassignment.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    @Query("SELECT c FROM Cart c WHERE c.user_id = :userId")  // 엔티티 필드명!
    Optional<Cart> findByUserId(@Param("userId") Long userId);
}
