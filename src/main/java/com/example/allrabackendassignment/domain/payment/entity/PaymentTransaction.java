package com.example.allrabackendassignment.domain.payment.entity;

import com.example.allrabackendassignment.domain.order.entity.Orders;
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
@Table(name = "payment_transaction", schema = "code_interview")
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id")
    private Long orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    private Orders order;

    @Column(name = "tid", nullable = false)
    private String tid;

    @Column(name = "pg_code", columnDefinition = "varchar(50) default 'credit_cart'")
    private String pgCode;

    @Column(name = "amount", nullable = false)
    private int amount;

    @Column(name = "discount_amount", columnDefinition = "int unsigned default 0")
    private int discountAmount;

    @Column(name = "use_point", columnDefinition = "int unsigned default 0")
    private int usePoint;

    @Column(name = "coupon_discount_amount", columnDefinition = "int unsigned default 0 ")
    private int couponDiscountAmount;

    @CreatedDate
    @Column(name = "created_at", updatable = false, columnDefinition = "timestamp default current_timestamp")
    private LocalDateTime createdAt;
}
