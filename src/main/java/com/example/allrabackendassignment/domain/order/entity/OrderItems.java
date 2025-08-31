package com.example.allrabackendassignment.domain.order.entity;

import com.example.allrabackendassignment.domain.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@Getter
@Builder
@DynamicUpdate
@DynamicInsert
@Table(name = "order_items", schema = "code_interview")
public class OrderItems {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    private Orders orders;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;

    @Column(name = "quantity", columnDefinition = "int unsigned not null")
    private int quantity;

    @Column(name = "unit_price", columnDefinition = "int unsigned not null")
    private int unitPrice;

    @Column(name = "total_price", columnDefinition = "int unsigned not null")
    private int totalPrice;

    @CreatedDate
    @Column(name = "created_at", updatable = false, columnDefinition = "timestamp default current_timestamp")
    private LocalDateTime createdAt;
}
