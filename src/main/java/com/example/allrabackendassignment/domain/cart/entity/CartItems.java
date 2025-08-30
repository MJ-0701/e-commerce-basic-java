package com.example.allrabackendassignment.domain.cart.entity;

import com.example.allrabackendassignment.domain.product.entity.Product;
import com.example.allrabackendassignment.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@Getter
@Builder
@DynamicUpdate
@DynamicInsert
@Table(name = "cart_items", schema = "code_interview")
public class CartItems extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cart_id")
    private Long cartId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", insertable = false, updatable = false)
    private Cart cart;

    @Column(name = "product_id")
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;

    @Column(name = "quantity", columnDefinition = "int unsigned default 1")
    private int quantity;

    public void increaseQuantity() {
        this.quantity += 1;
    }
    public void decreaseQuantity() {
        this.quantity -= 1;
    }

    public void updateQuantity(int quantity) {
        this.quantity = quantity;
    }
}
